package CS4337.Project;

import CS4337.Project.Shared.DTO.ReqBan;
import CS4337.Project.Shared.DTO.ShopBan;
import CS4337.Project.Shared.DTO.UserBan;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class BanService {
  private String userServiceUrl = "http://USERSERVICE/user/ban/{id}";
  private String shopServiceUrl = "http://SHOPSERVICE/shopItem/ban/{id}";

  @Autowired
  @Qualifier("msRestTemplate") private final RestTemplate restTemplate = new RestTemplate();

  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(BanService.class, args);
  }

  @PostMapping("/user/check")
  public ResponseEntity<Map<String, ? extends Object>> checkUserBans() {
    Date now = new Date(System.currentTimeMillis());
    try {
      List<UserBan> users =
          jdbcTemplate.query(
              "SELECT * FROM `UserBan` WHERE suspendedUntil <= ? AND isActive = TRUE",
              new UserBanMapper(),
              now);
      if (users.size() < 1) {
        return ResponseEntity.ok(Map.of("success", "no users to unban"));
      }
      Map<String, String> userUnbanJsonReq = Map.of("isHidden", "false");
      Map<String, ArrayList<Integer>> returnJson = new HashMap<>();

      returnJson.put("failed to unban", new ArrayList<>());
      returnJson.put("unbanned", new ArrayList<>());
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<Object> requestEntity = new HttpEntity<>(userUnbanJsonReq, headers);
      // loop through the list and try to unhide each user and store results to
      // inform poster
      for (UserBan u : users) {
        ResponseEntity<Map<String, Object>> resJson =
            restTemplate.exchange(
                this.userServiceUrl,
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                u.getUserId());
        if (resJson.getStatusCode() != HttpStatus.OK) {
          returnJson.get("failed to unban").add(u.getUserId());
        } else {
          returnJson.get("unbanned").add(u.getUserId());
          jdbcTemplate.update("UPDATE `UserBan` SET isActive = FALSE WHERE id = ?", u.getId());
        }
      }

      return ResponseEntity.ok(returnJson);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shopItem/check")
  public ResponseEntity<Map<String, ? extends Object>> checkShopBans() {
    Date now = new Date(System.currentTimeMillis());
    try {
      List<ShopBan> shopIs =
          jdbcTemplate.query(
              "SELECT * FROM `ShopItemBan` WHERE suspendedUntil <= ? AND isActive = TRUE",
              new ShopBanMapper(),
              now);
      if (shopIs.size() < 1) {
        return ResponseEntity.ok(Map.of("success", "no shop items to unban"));
      }
      Map<String, String> shopIUnbanJsonReq = Map.of("isHidden", "false");
      Map<String, ArrayList<Integer>> returnJson = new HashMap<>();

      returnJson.put("failed to unban", new ArrayList<>());
      returnJson.put("unbanned", new ArrayList<>());
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<Object> requestEntity = new HttpEntity<>(shopIUnbanJsonReq, headers);
      // loop through the list and try to unhide each user and store results to
      // inform poster
      for (ShopBan u : shopIs) {
        ResponseEntity<Map<String, Object>> resJson =
            restTemplate.exchange(
                this.shopServiceUrl,
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                u.getShopItemId());
        if (resJson.getStatusCode() != HttpStatus.OK) {
          returnJson.get("failed to unban").add(u.getShopItemId());
        } else {
          returnJson.get("unbanned").add(u.getShopItemId());
          jdbcTemplate.update("UPDATE `ShopItemBan` SET isActive = FALSE WHERE id = ?", u.getId());
        }
      }

      return ResponseEntity.ok(returnJson);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user")
  public ResponseEntity<Map<String, Object>> getUsersBans(
      @RequestParam(defaultValue = "0")
          int lastId, // using a cursor, when getting a batch other than the first you send the last
      // id you recived the last page
      @RequestParam(defaultValue = "50") int pageSize) {

    int maxItemsShown = 50;

    if (pageSize > maxItemsShown) {
      pageSize = 50;
    }
    try {
      List<UserBan> users =
          jdbcTemplate.query(
              "SELECT * FROM `UserBan` WHERE id > ? ORDER BY id LIMIT ?",
              new UserBanMapper(),
              lastId,
              pageSize);

      return ResponseEntity.ok(Map.of("success", users));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<Map<String, Object>> getUserBans(@PathVariable("id") int id) {
    try {
      List<UserBan> users =
          jdbcTemplate.query("SELECT * FROM `UserBan` WHERE userId = ?", new UserBanMapper(), id);

      return ResponseEntity.ok(Map.of("success", users));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/user")
  public ResponseEntity<Map<String, Object>> banUser(@RequestBody ReqBan banInfo) {
    if (banInfo.getBannedId() == 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "0 is not a valid bannedId"));
    }
    Map<String, String> userBanJsonReq = Map.of("isHidden", "true");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<Object> requestEntity = new HttpEntity<>(userBanJsonReq, headers);

    try {

      assert banInfo.getSuspendedUntil() != null;
      ResponseEntity<Map<String, Object>> resJson =
          restTemplate.exchange(
              this.userServiceUrl,
              HttpMethod.PUT,
              requestEntity,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              banInfo.getBannedId());

      if (resJson.getStatusCode() != HttpStatus.OK) {
        return resJson;
      }
      String sqlInsert =
          "INSERT INTO `UserBan` (userId,suspendedUntil,isActive) " + "VALUES (?, ?, TRUE)";
      int success =
          jdbcTemplate.update(sqlInsert, banInfo.getBannedId(), banInfo.getSuspendedUntil());
      return ResponseEntity.ok(Map.of("success", success));
    } catch (DataAccessException | HttpClientErrorException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shopItem")
  public ResponseEntity<Map<String, Object>> getShops(
      @RequestParam(defaultValue = "0")
          int lastId, // using a cursor, when getting a batch other than the first you send the last
      // id you recived the last page
      @RequestParam(defaultValue = "50") int pageSize) {

    int maxItemsShown = 50;

    if (pageSize > maxItemsShown) {
      pageSize = 50;
    }
    try {
      List<ShopBan> shops =
          jdbcTemplate.query(
              "SELECT * FROM `ShopItemBan` WHERE id > ? ORDER BY id LIMIT ?",
              new ShopBanMapper(),
              lastId,
              pageSize);
      return ResponseEntity.ok(Map.of("success", shops));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shopItem/{id}")
  public ResponseEntity<Map<String, Object>> getShopBans(@PathVariable("id") int id) {
    try {
      List<ShopBan> shop =
          jdbcTemplate.query(
              "SELECT * FROM `ShopItemBan` WHERE shopItemId = ?", new ShopBanMapper(), id);

      return ResponseEntity.ok(Map.of("success", shop));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shopItem")
  public ResponseEntity<Map<String, Object>> banShop(@RequestBody ReqBan banInfo) {
    if (banInfo.getBannedId() == 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "0 is not a valid bannedId"));
    }

    Map<String, String> shopBanJsonReq = Map.of("isHidden", "true");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<Object> requestEntity = new HttpEntity<>(shopBanJsonReq, headers);

    try {

      assert banInfo.getSuspendedUntil() != null;
      ResponseEntity<Map<String, Object>> resJson =
          restTemplate.exchange(
              this.shopServiceUrl,
              HttpMethod.PUT,
              requestEntity,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              banInfo.getBannedId());

      if (resJson.getStatusCode() != HttpStatus.OK) {
        return resJson;
      }

      String sqlInsert =
          "INSERT INTO `ShopItemBan` (shopItemId,suspendedUntil,isActive) " + "VALUES (?, ?, TRUE)";
      int success =
          jdbcTemplate.update(sqlInsert, banInfo.getBannedId(), banInfo.getSuspendedUntil());

      return ResponseEntity.ok(Map.of("success", success));
    } catch (DataAccessException | HttpClientErrorException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }
}
