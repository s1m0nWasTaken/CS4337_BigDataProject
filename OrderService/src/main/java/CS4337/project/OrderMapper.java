package CS4337.project;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class OrderMapper implements RowMapper<Order> {
  @Override
  public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
    Order order = new Order();
    order.setId(rs.getInt("id"));
    order.setOrderStatus(OrderStatus.valueOf(rs.getString("orderStatus")));
    order.setDeliveryAddress(rs.getString("deliveryAddress"));
    order.setShopItemid(rs.getInt("shopItemid"));
    order.setTransactionid(rs.getInt("transactionid"));
    order.setPrice(rs.getDouble("price"));
    order.setUserId(rs.getInt("userid"));
    return order;
  }
}
