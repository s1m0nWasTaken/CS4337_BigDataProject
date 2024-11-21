package CS4337.Project.Shared;

import java.sql.Timestamp;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RefreshToken {
  private int id;
  private int userId;
  private String refreshToken;
  private Timestamp expiryDate;
}
