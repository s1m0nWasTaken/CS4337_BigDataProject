package CS4337.Project.RatingService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepository {
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public RatingRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public int addRating(int shopid, int userid, String message, int rating) {
    String sql = "INSERT INTO ShopRating(shopid,userid,message,rating) VALUES(?,?,?,?)";
    return jdbcTemplate.update(sql, shopid, userid, message, rating);
  }

  public List<Rating> getRatingByShopId(int shopid) {
    String sql = "SELECT * FROM ShopRating WHERE shopid = ?";
    return jdbcTemplate.query(sql, new Object[] {shopid}, this::mapRowToRating);
  }

  private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
    Rating rating = new Rating();
    rating.setId(rs.getInt("id"));
    rating.setShopid(rs.getInt("shopid"));
    rating.setUserid(rs.getInt("userid"));
    rating.setMessage(rs.getString("message"));
    rating.setRating(rs.getInt("rating"));
    return rating;
  }
}
