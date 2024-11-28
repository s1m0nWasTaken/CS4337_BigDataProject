package CS4337.Project.Shared.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  @Value("${secret-key}")
  String secretKey;

  public boolean validateToken(String token, String username) {
    final String extractedUsername = extractUserId(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }

  public String extractUserId(String token) {
    return extractAllClaims(token).getSubject();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }

  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  public String extractRole(String token) {
    return (String) extractAllClaims(token).get("role");
  }
}
