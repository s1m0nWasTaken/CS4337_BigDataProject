package CS4337.Project;

import CS4337.Project.Model.GoogleTokenInfo;
import CS4337.Project.Model.GoogleUserInfo;
import CS4337.Project.Model.User;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
  private final AuthService authService;
  private final AuthRepository authRepository;

  public AuthController(AuthService authService, AuthRepository authRepository) {
    this.authService = authService;
    this.authRepository = authRepository;
  }

  // https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=http://localhost:8081/grantcode&response_type=code&client_id=529138320852-h26t99u2jh694u7q3u3c2oaqma07oabe.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&access_type=offline
  @GetMapping("/grantcode")
  public ResponseEntity<String> grantCode(
      @RequestParam("code") String code,
      @RequestParam("scope") String scope,
      @RequestParam("authuser") String authUser,
      @RequestParam("prompt") String prompt) {

    GoogleTokenInfo tokenInfo = authService.getGoogleTokenInfoByCode(code);
    GoogleUserInfo googleUserInfo = authService.getUserInfoFromGoogle(tokenInfo.getAccessToken());
    User user = authService.getUserByEmail(googleUserInfo.getEmail());

    if (user == null) {
      user = authService.createUserFromGoogleUserInfo(googleUserInfo);
      String registerResponse = authService.registerUser(user);
      user = authService.getUserByEmail(user.getEmail());

      if (registerResponse.contains("success")
          && user != null
          && authRepository.addTokens(tokenInfo, user.getId()) > 0) {
        return ResponseEntity.status(HttpStatus.CREATED).body("User registration successful");
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("User registration failed: " + registerResponse);
      }

    } else if (authRepository.updateTokens(tokenInfo, user.getId()) > 0) {
      return ResponseEntity.status(HttpStatus.OK).body("Login successful");
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refresh_token");

    int userId = authRepository.getUserIdByRefreshToken(refreshToken);
    if (userId == -1) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Refresh token expired or not found");
    }

    GoogleTokenInfo newTokenInfo = authService.refreshTokens(refreshToken);

    if (authRepository.updateTokens(newTokenInfo, userId) > 0) {
      return ResponseEntity.status(HttpStatus.OK).body("Tokens refreshed successfully");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to refresh tokens");
    }
  }
}
