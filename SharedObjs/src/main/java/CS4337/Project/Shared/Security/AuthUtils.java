package CS4337.Project.Shared.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
  public static boolean isUserAdmin() {
    String role = getUserRole();
    return role.equalsIgnoreCase("ROLE_admin");
  }

  public static String getUserRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);
    return role;
  }

  public static int getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return Integer.parseInt((String) authentication.getPrincipal());
  }
}
