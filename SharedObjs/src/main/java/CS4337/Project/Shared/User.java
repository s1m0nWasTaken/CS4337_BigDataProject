package CS4337.Project.Shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class User {
  @JsonProperty("id")
  private int id;

  @JsonProperty("userType")
  private UserType userType;

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("address")
  private String address;

  @JsonProperty("isHidden")
  private boolean isHidden;
}
