package CS4337.Project;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class ShopService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(ShopService.class, args);
  }

  @GetMapping("/shop")
  public List<Map<String, Object>> shop() {
    List<Map<String, Object>> shops;
    try {
      shops = jdbcTemplate.queryForList("SELECT * FROM Shop");
    } catch (DataAccessException e) {
      jdbcTemplate.execute("USE user_service;");
      jdbcTemplate.execute(
          "CREATE TABLE `ShopItem` ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "shopOwnerid INT NOT NULL, "
              + "shopName VARCHAR(255) NOT NULL, "
              + "imageData VARCHAR(255), "
              + "description VARCHAR(255), "
              + "shopType ENUM(CLOTHING, ELECTRONICS,FOOD, BOOKS, TOYS, OTHER) NOT NULL, "
              + "shopEmail VARCHAR (255) NOT NULL);");
      shops = jdbcTemplate.queryForList(" SELECT * FROM Shop ");
    }
    return shops;
  }

  @PostMapping("/shop")
  public Map<String, Object> addShop(@RequestBody Shop shop) {
    // needs user access checking
    try {
      SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate);
      String sqlInsert =
          "INSERT INTO 'Shop' (shopOwnerid, shopName, imageData, description, shopType, shopEmail) "
              + "VALUES (?, ?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          sqlInsert,
          shop.getShopOwnerid(),
          shop.getShopName(),
          shop.getImageData(),
          shop.getDescription(),
          shop.getShopType().name(),
          shop.getShopEmail());
      return Map.of("success", 1);
    } catch (TransientDataAccessResourceException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @GetMapping("/shopitem")
  public List<Map<String, Object>> shopItem() {
    List<Map<String, Object>> shopItems;
    try {
      shopItems = jdbcTemplate.queryForList("SELECT * FROM ShopItem");
    } catch (DataAccessException e) {
      jdbcTemplate.execute("USE user_service;");
      jdbcTemplate.execute(
          "CREATE TABLE `ShopItem` ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "shopid INT NOT NULL, "
              + "price DOUBLE(10, 2), "
              + "stock INT NOT NULL"
              + "picture VARCHAR(255), "
              + "description VARCHAR(255), "
              + "canUpdate BOOL DEFAULT TRUE, "
              + "isHidden BOOL DEFAULT TRUE);");
      shopItems = jdbcTemplate.queryForList(" SELECT * FROM ShopItem ");
    }
    return shopItems;
  }

  @PostMapping("/shopItem")
  public Map<String, Object> addShopItem(@RequestBody ShopItem shopItem) {
    // need to add user access checking
    try {
      SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate);
      String sqlInsert =
          "INSERT INTO 'ShopItem' (shopid, price, stock, picture, description) "
              + "VALUES (?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          sqlInsert,
          shopItem.getShopid(),
          shopItem.getPrice(),
          shopItem.getStock(),
          shopItem.getPicture(),
          shopItem.getDescription());
      return Map.of("success", 1);
    } catch (TransientDataAccessResourceException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @PutMapping("/shopItem/{id}")
  public Map<String, Object> updateShopItem(@PathVariable int id, @RequestBody ShopItem shopItem) {
    // need to add user access checking

    String updateQuery =
        "UPDATE ShopItem SET price = ?, stock = ?, picture = ?, description = ? WHERE id = ?";

    try {
      int rowsAffected =
          jdbcTemplate.update(
              updateQuery,
              shopItem.getShopid(),
              shopItem.getPrice(),
              shopItem.getStock(),
              shopItem.getPicture(),
              shopItem.getDescription(),
              id);

      if (rowsAffected > 0) {
        return Map.of("success", 1, "message", "Shop item updated successfully");
      } else {
        return Map.of("success", 0, "message", "Shop item not found");
      }
    } catch (DataAccessException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @PutMapping("/shop/{id}")
  public Map<String, Object> updateShop(@PathVariable int id, @RequestBody Shop shop) {
    // add checking for only shopOwnerId allocated to shop allowed to update
    // shop
    String updateQuery =
        "UPDATE Shop SET shopOwnerid = ?, shopName = ?, imageData = ?, description = ?, shopType = ?, shopEmail = ? WHERE id = ?";

    try {
      int rowsAffected =
          jdbcTemplate.update(
              updateQuery,
              shop.getShopOwnerid(),
              shop.getShopName(),
              shop.getImageData(),
              shop.getDescription(),
              shop.getShopType().name(),
              shop.getShopEmail(),
              id);

      if (rowsAffected > 0) {
        return Map.of("success", 1, "message", "Shop updated successfully");
      } else {
        return Map.of("success", 0, "message", "Shop not found");
      }
    } catch (DataAccessException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @DeleteMapping("/shop/{id}")
  public Map<String, Object> deleteShop(@PathVariable int id) {
    String deleteQuery = "DELETE FROM Shop WHERE id = ?";

    try {
      int rowsAffected = jdbcTemplate.update(deleteQuery, id);

      if (rowsAffected > 0) {
        return Map.of("success", 1, "message", "Shop deleted successfully");
      } else {
        return Map.of("success", 0, "message", "Shop not found");
      }
    } catch (DataAccessException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @DeleteMapping("/shopItem/{id}")
  public Map<String, Object> deleteShopItem(@PathVariable int id) {
    String deleteQuery = "DELETE FROM ShopItem WHERE id = ?";

    try {
      int rowsAffected = jdbcTemplate.update(deleteQuery, id);

      if (rowsAffected > 0) {
        return Map.of("success", 1, "message", "Shop item deleted successfully");
      } else {
        return Map.of("success", 0, "message", "Shop item not found");
      }
    } catch (DataAccessException e) {
      return Map.of("error", e.getMessage());
    }
  }

  @PutMapping("shopItem/ban/{id}")
  public ResponseEntity<Map<String, Object>> banShopItem(
      @PathVariable("id") int id, @RequestBody Map<String, String> requestBody) {
    try {
      String hiddenStr = requestBody.get("isHidden");
      Boolean hidden = (hiddenStr.equals("true") ? true : false);
      String banStatement = "UPDATE `ShopItem` SET isHidden = ? WHERE id = ?";
      int sucess = jdbcTemplate.update(banStatement, hidden, id);
      if (sucess == 1) {
        return ResponseEntity.ok(Map.of("Shop item hidden", hidden));
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }
}
