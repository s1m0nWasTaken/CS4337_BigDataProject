package CS4337.Project;

import CS4337.Project.Model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {
  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String redirectUri;

  private String userServiceUrl = "http://USERSERVICE";

  @Value("${secret-key}")
  private String secretKey;

  @Autowired private RestTemplate restTemplate;
  @Autowired private AuthRepository authRepository;

  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final int JWT_EXPIRY = 3600 * 1000;
  private static final long REFRESH_TOKEN_EXPIRY = 10 * 86400 * 1000; // expires in 10 days

  public GoogleTokenInfo getGoogleTokenInfoByCode(String code) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("redirect_uri", redirectUri);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
    params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
    params.add("scope", "openid");
    params.add("grant_type", "authorization_code");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

    GoogleTokenInfo tokenInfo =
        restTemplate.postForObject(TOKEN_URL, requestEntity, GoogleTokenInfo.class);
    return tokenInfo;
  }

  public GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
    ResponseEntity<GoogleUserInfo> response =
        restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, GoogleUserInfo.class);

    return response.getBody();
  }

  public User getUserByEmail(String email) {
    String url = userServiceUrl + "/user/email/" + email;
    return getUser(url);
  }

  public User getUserById(int id) {
    String url = userServiceUrl + "/user/" + id;
    return getUser(url);
  }

  public User createUserFromGoogleUserInfo(GoogleUserInfo googleUserInfo) {
    User user = new User();

    user.setEmail(googleUserInfo.getEmail());
    user.setUsername(googleUserInfo.getName());
    user.setUserType(UserType.customer); // todo: figure out how to register different types of user
    user.setHidden(false);

    return user;
  }

  public ResponseEntity<?> registerUser(User user) {
    String postUserUrl = userServiceUrl + "/users";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<User> requestEntity = new HttpEntity<>(user, httpHeaders);

    String response = restTemplate.postForObject(postUserUrl, requestEntity, String.class);
    user = getUserByEmail(user.getEmail());

    if (!response.contains("success") || user == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User registration failed");
    }

    String jwt = generateJWT(user);
    RefreshToken refreshToken = generateRefreshToken(user);

    if (authRepository.addRefreshToken(refreshToken) > 0) {
      Map<String, String> tokenResponse = new HashMap<>();
      tokenResponse.put("accessToken", jwt);
      tokenResponse.put("refreshToken", refreshToken.getRefreshToken());
      return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User registration failed");
  }

  public ResponseEntity<?> generateTokens(User user) {
    String jwt = generateJWT(user);
    RefreshToken refreshToken = generateRefreshToken(user);

    if (authRepository.updateOrAddRefreshToken(refreshToken) > 0) {
      Map<String, String> tokenResponse = generateTokenResponseMap(jwt, refreshToken);
      return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate user");
  }

  private User getUser(String url) {
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> successMap = (Map<String, Object>) responseBody.get("success");

        if (successMap != null) {
          ObjectMapper objectMapper = new ObjectMapper();
          User user = objectMapper.convertValue(successMap, User.class);
          return user;
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public String generateJWT(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("role", user.getUserType().name())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRY))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
  }

  private RefreshToken generateRefreshToken(User user) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUserId(user.getId());
    refreshToken.setRefreshToken(UUID.randomUUID().toString());
    refreshToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY));

    return refreshToken;
  }

  private Map<String, String> generateTokenResponseMap(
      String accessToken, RefreshToken refreshToken) {
    Map<String, String> responseMap = new HashMap<>();
    responseMap.put("accessToken", accessToken);
    responseMap.put("refreshToken", refreshToken.getRefreshToken());
    return responseMap;
  }
}
