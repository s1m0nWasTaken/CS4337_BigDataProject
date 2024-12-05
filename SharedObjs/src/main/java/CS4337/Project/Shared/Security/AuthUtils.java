package CS4337.Project.Shared.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
  public static boolean isUserAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    if (role.equalsIgnoreCase("ROLE_admin")) {
      return true;
    }

    return false;
  }

  public static int getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return Integer.parseInt((String) authentication.getPrincipal());
  }
}
