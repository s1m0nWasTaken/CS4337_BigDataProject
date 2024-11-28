package CS4337.Project;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ShopItemRowMapper implements RowMapper<ShopItem> {

  @Override
  public ShopItem mapRow(ResultSet rs, int rowNum) throws SQLException {
    ShopItem shopItem = new ShopItem();

    // Mapping each column to the corresponding field in the ShopItem class
    shopItem.setId(rs.getInt("id"));
    shopItem.setShopid(rs.getInt("shopid"));
    shopItem.setPrice(rs.getDouble("price"));
    shopItem.setItemName(rs.getString("itemName"));
    shopItem.setStock(rs.getInt("stock"));
    shopItem.setDescription(rs.getString("description"));
    shopItem.setPicture(rs.getString("picture"));
    shopItem.setHidden(rs.getBoolean("isHidden"));
    shopItem.setCanUpdate(rs.getBoolean("canUpdate"));
    return shopItem;
  }
}
