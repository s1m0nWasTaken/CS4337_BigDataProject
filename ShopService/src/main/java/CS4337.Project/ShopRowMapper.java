package CS4337.Project;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ShopRowMapper implements RowMapper<Shop> {

  @Override
  public Shop mapRow(ResultSet rs, int rowNum) throws SQLException {
    Shop shop = new Shop();

    shop.setId(rs.getInt("id"));
    shop.setShopOwnerid(rs.getInt("shopOwnerid"));
    shop.setShopName(rs.getString("shopName"));
    shop.setDescription(rs.getString("description"));
    shop.setImageData(rs.getString("imageData"));
    shop.setShopType(ShopType.valueOf(rs.getString("shopType")));
    shop.setShopEmail(rs.getString("shopEmail"));
    return shop;
  }
}
