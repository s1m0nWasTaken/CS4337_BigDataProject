package CS4337.Project.Shared.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class GoogleTokenInfo {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("expires_in")
  private int expiresIn;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("scope")
  private String scope;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("id_token")
  private String idToken;
}
