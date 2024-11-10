package CS4337.Project;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class ShopService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(ShopService.class, args);
  }

  @GetMapping("/shop")
  public ResponseEntity<Map<String, Object>> shop() {
    List<Map<String, Object>> shops;
    try {
      shops = jdbcTemplate.queryForList("SELECT * FROM Shop");
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", shops));
  }

  @PostMapping("/shop")
  public Map<String, Object> addShop(@RequestBody Shop shop) {
    // needs user access checking
    try {
      String sqlInsert =
          "INSERT INTO Shop (shopOwnerid, shopName, imageData, description, shopType, shopEmail) "
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

  @GetMapping("/shopItem")
  public ResponseEntity<Map<String, Object>> shopItem() {
    List<Map<String, Object>> shopItems;
    try {
      shopItems = jdbcTemplate.queryForList("SELECT * FROM ShopItem");
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", shopItems));
  }

  @PostMapping("/shopItem")
  public Map<String, Object> addShopItem(@RequestBody ShopItem shopItem) {
    // need to add user access checking
    try {
      String sqlInsert =
          "INSERT INTO ShopItem (shopid, price, itemName, stock, picture, description) "
              + "VALUES (?, ?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          sqlInsert,
          shopItem.getShopid(),
          shopItem.getPrice(),
          shopItem.getItemName(),
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

    List<Object> params = new ArrayList<>();
    StringBuilder sql = new StringBuilder("UPDATE ShopItem SET ");

    if (shopItem.getPrice() != -1) {
      sql.append("price = ?, ");
      params.add(shopItem.getPrice());
    }

    if (shopItem.getItemName() != null) {
      sql.append("itemName = ?, ");
      params.add(shopItem.getItemName());
    }

    if (shopItem.getStock() != -1) {
      sql.append("stock = ?, ");
      params.add(shopItem.getStock());
    }

    if (shopItem.getPicture() != null) {
      sql.append("picture = ?, ");
      params.add(shopItem.getPicture());
    }

    if (shopItem.getDescription() != null) {
      sql.append("description = ?, ");
      params.add(shopItem.getDescription());
    }

    sql.setLength(sql.length() - 2);
    sql.append(" WHERE id = ?");
    params.add(id);

    try {
      int rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());

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

    List<Object> params = new ArrayList<>();
    StringBuilder sql = new StringBuilder("UPDATE Shop SET ");

    if (shop.getShopOwnerid() != -1) {
      sql.append("shopOwnerid = ?, ");
      params.add(shop.getShopOwnerid());
    }

    if (shop.getShopName() != null) {
      sql.append("shopName = ?, ");
      params.add(shop.getShopName());
    }

    if (shop.getImageData() != null) {
      sql.append("imageData = ?, ");
      params.add(shop.getImageData());
    }

    if (shop.getDescription() != null) {
      sql.append("description = ?, ");
      params.add(shop.getDescription());
    }

    if (shop.getShopType() != null) {
      sql.append("shopType = ?, ");
      params.add(shop.getShopType().name());
    }

    if (shop.getShopEmail() != null) {
      sql.append("shopEmail = ?, ");
      params.add(shop.getShopEmail());
    }

    sql.setLength(sql.length() - 2);
    sql.append(" WHERE id = ?");
    params.add(id);

    try {
      int rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());

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
