package CS4337.Project.Shared.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class GoogleUserInfo {
  private String sub; // UUID
  private String name;
  private String givenName;
  private String familyName;
  private String picture;
  private String email;
  private boolean emailVerified;
  private String locale;
}
