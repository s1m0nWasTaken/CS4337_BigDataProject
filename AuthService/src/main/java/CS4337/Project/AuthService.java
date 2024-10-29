package CS4337.Project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public String getOauthAccessTokenGoogle(String code) {
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

        String tokenUrl = "https://oauth2.googleapis.com/token";
        String response = restTemplate.postForObject(tokenUrl, requestEntity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse access token", e);
        }
    }

    public GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, GoogleUserInfo.class);

        return response.getBody();
    }

    public boolean userExistsByEmail(String email) {
        String getUserUrl = userServiceUrl + "/user/" + email;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(getUserUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful() && response.getBody().containsKey("success");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return false;
            }
            throw e;
        }
    }

    public User createUserFromGoogleUserInfo(GoogleUserInfo googleUserInfo) {
        User user = new User();

        user.setEmail(googleUserInfo.getEmail());
        user.setUsername(googleUserInfo.getName());
        user.setUserType(UserType.customer); // todo: make this so we can register other types of user
        user.setIsHidden(false);

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
}
