package CS4337.Project;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

//  @Value("${secret-key}")
  private String secretKey = "Rlzt4oNqcVIT1m5lpEDdxYAikUaCxrphvaQAaxVwv8M=";

  public boolean validateToken(String token, String email) {
    final String extractedUserEmail = extractUserEmail(token);
    return (extractedUserEmail.equals(email) && !isTokenExpired(token));
  }

  public String extractUserEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  public String extractRole(String token) {
    return (String) extractAllClaims(token).get("role");
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }

  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }
}
