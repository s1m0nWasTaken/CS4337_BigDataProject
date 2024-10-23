package CS4337.Project;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BanService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(BanService.class, args);
  }

  @GetMapping("/users")
  public ResponseEntity<Map<String, Object>> getUsersBans() {
    try {
      List<UserBan> users =
          jdbcTemplate.query("SELECT * FROM `UserBan`", new UserBanMapper());

      return ResponseEntity.ok(Map.of("success", users));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<Map<String, Object>>
  getUserBans(@PathVariable("id") int id) {
    try {
      List<UserBan> users = jdbcTemplate.query(
          "SELECT * FROM `UserBan` WHERE userId= ?", new UserBanMapper());

      return ResponseEntity.ok(Map.of("success", users));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/users")
  public ResponseEntity<Map<String, Object>>
  banUser(@RequestBody ReqBan banInfo) {
    try {
      String sqlInsert = "INSERT INTO `UserBan` (userId,suspendedUntil) "
                         + "VALUES (?, ?)";
      int success = jdbcTemplate.update(sqlInsert, banInfo.getBanedId(),
                                        banInfo.getSuspendedUntil());

      return ResponseEntity.ok(Map.of("success", success));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shops")
  public ResponseEntity<Map<String, Object>> getShops() {
    try {
      List<ShopBan> shops =
          jdbcTemplate.query("SELECT * FROM `ShopBan`", new ShopBanMapper());
      return ResponseEntity.ok(Map.of("success", shops));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shops/{id}")
  public ResponseEntity<Map<String, Object>>
  getShopBans(@PathVariable("id") int id) {
    try {
      List<ShopBan> shop = jdbcTemplate.query(
          "SELECT * FROM `UserBan` WHERE shopId= ?", new ShopBanMapper());

      return ResponseEntity.ok(Map.of("success", shop));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shops")
  public ResponseEntity<Map<String, Object>>
  banShop(@RequestBody ReqBan banInfo) {
    try {
      String sqlInsert = "INSERT INTO `ShopBan` (shopId,suspendedUntil) "
                         + "VALUES (?, ?)";
      int success = jdbcTemplate.update(sqlInsert, banInfo.getBanedId(),
                                        banInfo.getSuspendedUntil());

      return ResponseEntity.ok(Map.of("success", success));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }
}
