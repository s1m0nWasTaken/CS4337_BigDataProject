package CS4337.Project;

import CS4337.Project.Shared.Models.User;
import CS4337.Project.Shared.Models.UserType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class UserRowMapper implements RowMapper<User> {
  @Override
  public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    User user = new User();
    user.setId(rs.getInt("id"));
    user.setUserType(UserType.valueOf(rs.getString("userType")));
    user.setUsername(rs.getString("username"));
    user.setEmail(rs.getString("email"));
    user.setAddress(rs.getString("address"));
    user.setHidden(rs.getBoolean("hidden"));
    return user;
  }
}
