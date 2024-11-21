package CS4337.Project;

import CS4337.Project.Shared.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
  @Autowired private JdbcTemplate jdbcTemplate;

  public int getUserIdByRefreshToken(String refreshToken) {
    String sql = "SELECT userId FROM Auth WHERE refreshToken = ? AND expiryDate > NOW()";
    try {
      return jdbcTemplate.queryForObject(sql, new Object[] {refreshToken}, Integer.class);
    } catch (DataAccessException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int addRefreshToken(RefreshToken refreshToken) {
    String sql = "INSERT INTO Auth(userId, refreshToken, expiryDate) VALUES (?, ?, ?)";

    return jdbcTemplate.update(
        sql,
        refreshToken.getUserId(),
        refreshToken.getRefreshToken(),
        refreshToken.getExpiryDate());
  }

  public int updateOrAddRefreshToken(RefreshToken refreshToken) {
    String sql = "UPDATE Auth SET refreshToken = ?, expiryDate = ? WHERE userId = ?";

    int updated =
        jdbcTemplate.update(
            sql,
            refreshToken.getRefreshToken(),
            refreshToken.getExpiryDate(),
            refreshToken.getUserId());

    if (updated > 0) {
      return updated;
    }

    return addRefreshToken(refreshToken);
  }
}
