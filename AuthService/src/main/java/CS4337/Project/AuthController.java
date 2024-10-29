package CS4337.Project;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=http://localhost:8081/grantcode&response_type=code&client_id=529138320852-h26t99u2jh694u7q3u3c2oaqma07oabe.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&access_type=offline
  @GetMapping("/grantcode")
  public ResponseEntity<String> grantCode(
      @RequestParam("code") String code,
      @RequestParam("scope") String scope,
      @RequestParam("authuser") String authUser,
      @RequestParam("prompt") String prompt) {

    String accessToken = authService.getOauthAccessTokenGoogle(code);
    GoogleUserInfo googleUserInfo = authService.getUserInfoFromGoogle(accessToken);

    if (!authService.userExistsByEmail(googleUserInfo.getEmail())) {
      User user = authService.createUserFromGoogleUserInfo(googleUserInfo);
      String registerResponse = authService.registerUser(user);

      if (registerResponse.contains("success")) {
        return ResponseEntity.status(HttpStatus.CREATED).body("User registration successful");
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("User registration failed: " + registerResponse);
      }

    } else {
      return ResponseEntity.status(HttpStatus.OK).body("Login successful");
    }
  }
}
