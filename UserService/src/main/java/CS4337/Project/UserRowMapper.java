package CS4337.Project;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class UserRowMapper implements RowMapper<User> {
  @Override
  public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    User user =
        new User(
            rs.getInt("id"),
            rs.getString("userType"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getString("suspendedUntil"));
    return user;
  }
} /*
  int id, UserType type, String username, String email, String address, Date suspendedUntil */
