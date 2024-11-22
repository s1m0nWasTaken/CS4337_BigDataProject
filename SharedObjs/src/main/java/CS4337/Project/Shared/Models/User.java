package CS4337.Project.Shared.Models;

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

  public User(
      int id, UserType userType, String username, String email, String address, boolean isHidden) {
    this.id = id;
    this.userType = userType;
    this.username = username;
    this.email = email;
    this.address = address;
    this.isHidden = isHidden;
  }

  public User(UserType userType, String username, String email, String address) {
    this.userType = userType;
    this.username = username;
    this.email = email;
    this.address = address;
  }

  public User(
      int id, String userType, String username, String email, String address, boolean isHidden) {
    this.id = id;
    this.userType = UserType.valueOf(userType);
    this.username = username;
    this.email = email;
    this.address = address;
    this.isHidden = isHidden;
  }

  public User(String userType, String username, String email, String address) {
    this.userType = UserType.valueOf(userType);
    this.username = username;
    this.email = email;
    this.address = address;
  }
}
