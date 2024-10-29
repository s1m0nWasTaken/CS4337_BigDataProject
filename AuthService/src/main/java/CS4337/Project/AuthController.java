package CS4337.Project;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AuthController {
  String clientId = "";

  String clientSecret = "";

  @GetMapping("/grantcode")
  public String grantCode(
      @RequestParam("code") String code,
      @RequestParam("scope") String scope,
      @RequestParam("authuser") String authUser,
      @RequestParam("prompt") String prompt) {
    return processGrantCode(code);
  }

  private String processGrantCode(String code) {
    return code;
  }

  private String getOauthAccessTokenGoogle(String code) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("redirect_uri", "http://localhost:8081/grantcode");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
    params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
    params.add("scope", "openid");
    params.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

    String url = "https://oauth2.googleapis.com/token";
    String response = restTemplate.postForObject(url, requestEntity, String.class);
    return response;
  }

  private void getProfileDetailsGoogle(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(accessToken);

    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

    String url = "https://www.googleapis.com/oauth2/v2/userinfo";
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    //    JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);
  }
}
