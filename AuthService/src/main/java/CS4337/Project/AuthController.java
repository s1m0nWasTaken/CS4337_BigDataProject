package CS4337.Project;

import CS4337.Project.Shared.DTO.GoogleTokenInfo;
import CS4337.Project.Shared.DTO.GoogleUserInfo;
import CS4337.Project.Shared.Models.User;
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

  // Use the below link to login via Google OAuth
  // https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=http://localhost:8082/grantcode&response_type=code&client_id=529138320852-h26t99u2jh694u7q3u3c2oaqma07oabe.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&access_type=offline
  @GetMapping("/grantcode")
  public ResponseEntity<?> grantCode(
      @RequestParam("code") String code,
      @RequestParam("scope") String scope,
      @RequestParam("authuser") String authUser,
      @RequestParam("prompt") String prompt) {
    GoogleTokenInfo tokenInfo = authService.getGoogleTokenInfoByCode(code);
    GoogleUserInfo googleUserInfo = authService.getUserInfoFromGoogle(tokenInfo.getAccessToken());
    User user = authService.getUserByEmail(googleUserInfo.getEmail());

    if (user == null) {
      user = authService.createUserFromGoogleUserInfo(googleUserInfo);
      return authService.registerUser(user);
    } else {
      return authService.generateTokens(user);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");

    int userId = authRepository.getUserIdByRefreshToken(refreshToken);
    if (userId == -1) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
    }

    User user = authService.getUserById(userId);
    return authService.generateTokens(user);
  }
}
