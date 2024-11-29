package CS4337.Project;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
  public boolean isUserAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    return role.equalsIgnoreCase("ROLE_admin");
  }

  public boolean isUserRatingOwner(int ratingOwnerId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int userId = Integer.parseInt((String) authentication.getPrincipal());
    return userId == ratingOwnerId;
  }
}
