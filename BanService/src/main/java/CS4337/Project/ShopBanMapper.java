package CS4337.Project;

import CS4337.Project.Shared.DTO.ShopBan;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class ShopBanMapper implements RowMapper<ShopBan> {
  @Override
  public ShopBan mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    ShopBan sBan =
        new ShopBan(
            rs.getInt("id"), rs.getInt("shopItemId"),
            rs.getDate("suspendedUntil"), rs.getBoolean("isActive"));
    return sBan;
  }
}
