package CS4337.Project;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class BanService {
  // TODO: replace with env vars
  private String userServiceUrl = "http://localhost:9090/user/ban/{id}";
  // fix this later
  private String shopServiceUrl = "http://localhost:9092/shop/ban/{id}";
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(BanService.class, args);
  }

  @PostMapping("/users/check")
  public ResponseEntity<Map<String, ? extends Object>> checkUserBans() {
    Date now = new Date(System.currentTimeMillis());
    try {
      List<UserBan> users = jdbcTemplate.query(
          "SELECT * FROM `UserBan` WHERE suspendedUntil <= ?",
          new UserBanMapper(), now);
      if (users.size() < 1) {
        return ResponseEntity.ok(Map.of("success", "no users to unban"));
      }
      Map<String, String> userUnbanJsonReq = Map.of("isHidden", "false");
      Map<String, Integer> returnJson = new HashMap<>();
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<Object> requestEntity =
          new HttpEntity<>(userUnbanJsonReq, headers);
      // loop through the list and try to unhide each user and store results to
      // inform poster
      for (UserBan u : users) {
        ResponseEntity<Map<String, Object>> resJson = restTemplate.exchange(
            userServiceUrl, HttpMethod.PUT, requestEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {},
            u.getUserId());
        if (resJson.getStatusCode() != HttpStatus.OK) {
          returnJson.put("failed to unban", u.getUserId());
        } else {
          returnJson.put("unbaned", u.getUserId());
        }
      }

      return ResponseEntity.ok(returnJson);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shops/check")
  public ResponseEntity<Map<String, ? extends Object>> checkShopBans() {
    Date now = new Date(System.currentTimeMillis());
    try {
      List<ShopBan> shops = jdbcTemplate.query(
          "SELECT * FROM `ShopBan` WHERE suspendedUntil <= ?",
          new ShopBanMapper(), now);
      if (shops.size() < 1) {
        return ResponseEntity.ok(Map.of("success", "no shops to unban"));
      }
      Map<String, String> shopUnbanJsonReq = Map.of("isHidden", "false");
      Map<String, Integer> returnJson = new HashMap<>();
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<Object> requestEntity =
          new HttpEntity<>(shopUnbanJsonReq, headers);
      // loop through the list and try to unhide each user and store results to
      // inform poster
      for (ShopBan s : shops) {
        ResponseEntity<Map<String, Object>> resJson = restTemplate.exchange(
            shopServiceUrl, HttpMethod.PUT, requestEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {},
            s.getShopId());
        if (resJson.getStatusCode() != HttpStatus.OK) {
          returnJson.put("failed to unban", s.getShopId());
        } else {
          returnJson.put("unbaned", s.getShopId());
        }
      }

      return ResponseEntity.ok(returnJson);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
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
