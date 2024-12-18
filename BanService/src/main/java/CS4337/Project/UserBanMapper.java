package CS4337.Project;

import CS4337.Project.Shared.DTO.UserBan;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class UserBanMapper implements RowMapper<UserBan> {
  @Override
  public UserBan mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    UserBan uBan =
        new UserBan(
            rs.getInt("id"), rs.getInt("userId"),
            rs.getDate("suspendedUntil"), rs.getBoolean("isActive"));
    return uBan;
  }
}
