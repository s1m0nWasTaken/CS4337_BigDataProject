package CS4337.Project;

import CS4337.Project.Model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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

  @Value("${user.service.url}")
  private String userServiceUrl;

  @Autowired private RestTemplate restTemplate;

  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

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
    String getUserUrl = userServiceUrl + "/user/" + email;

    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(getUserUrl, Map.class);

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

  public User createUserFromGoogleUserInfo(GoogleUserInfo googleUserInfo) {
    User user = new User();

    user.setEmail(googleUserInfo.getEmail());
    user.setUsername(googleUserInfo.getName());
    user.setUserType(UserType.customer); // todo: figure out how to register different types of user
    user.setHidden(false);

    return user;
  }

  public String registerUser(User user) {
    String postUserUrl = userServiceUrl + "/users";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<User> requestEntity = new HttpEntity<>(user, httpHeaders);

    String response = restTemplate.postForObject(postUserUrl, requestEntity, String.class);
    return response;
  }

  public GoogleTokenInfo refreshTokens(String refreshToken) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("refresh_token", refreshToken);
    params.add("grant_type", "refresh_token");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

    GoogleTokenInfo tokenInfo =
        restTemplate.postForObject(TOKEN_URL, requestEntity, GoogleTokenInfo.class);
    return tokenInfo;
  }
}
