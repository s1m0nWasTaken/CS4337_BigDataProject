package CS4337.Project;

import CS4337.Project.Model.GoogleTokenInfo;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
  @Autowired private JdbcTemplate jdbcTemplate;

  private static final long DAYS_UNTIL_REFRESH_TOKEN_EXPIRY = 10;

  public int getUserIdByRefreshToken(String refreshToken) {
    String sql = "SELECT userId FROM Auth WHERE refreshToken = ? AND refreshTokenExpiry > NOW()";
    try {
      return jdbcTemplate.queryForObject(sql, new Object[] {refreshToken}, Integer.class);
    } catch (DataAccessException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int updateTokens(GoogleTokenInfo tokenInfo, int userId) {
    LocalDateTime now = LocalDateTime.now();
    String accessToken = tokenInfo.getAccessToken();
    Timestamp accessTokenExpiry = Timestamp.valueOf(now.plusSeconds(tokenInfo.getExpiresIn()));
    String refreshToken = tokenInfo.getRefreshToken();
    Timestamp refreshTokenExpiry = Timestamp.valueOf(now.plusDays(DAYS_UNTIL_REFRESH_TOKEN_EXPIRY));

    String sql =
        "UPDATE Auth SET accessToken = ?, accessTokenExpiry = ?, refreshToken = ?, refreshTokenExpiry = ? WHERE userId = ?";

    return jdbcTemplate.update(
        sql, accessToken, accessTokenExpiry, refreshToken, refreshTokenExpiry, userId);
  }

  public int updateAccessToken(GoogleTokenInfo tokenInfo, int userId) {
    LocalDateTime now = LocalDateTime.now();
    String accessToken = tokenInfo.getAccessToken();
    Timestamp accessTokenExpiry = Timestamp.valueOf(now.plusSeconds(tokenInfo.getExpiresIn()));

    String sql = "UPDATE Auth SET accessToken = ?, accessTokenExpiry = ? WHERE userId = ?";

    return jdbcTemplate.update(sql, accessToken, accessTokenExpiry, userId);
  }

  public int addTokens(GoogleTokenInfo tokenInfo, int userId) {
    LocalDateTime now = LocalDateTime.now();
    String accessToken = tokenInfo.getAccessToken();
    Timestamp accessTokenExpiry = Timestamp.valueOf(now.plusSeconds(tokenInfo.getExpiresIn()));
    String refreshToken = tokenInfo.getRefreshToken();
    Timestamp refreshTokenExpiry = Timestamp.valueOf(now.plusDays(DAYS_UNTIL_REFRESH_TOKEN_EXPIRY));

    String sql =
        "INSERT INTO Auth(userId, accessToken, accessTokenExpiry, refreshToken, refreshTokenExpiry) VALUES (?, ?, ?, ?, ?)";

    return jdbcTemplate.update(
        sql, userId, accessToken, accessTokenExpiry, refreshToken, refreshTokenExpiry);
  }
}
