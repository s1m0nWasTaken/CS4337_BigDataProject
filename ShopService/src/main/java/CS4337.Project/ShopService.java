package CS4337.Project;

import CS4337.Project.Shared.Models.ShopItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class ShopService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(ShopService.class, args);
  }

  @GetMapping("/shop")
  public ResponseEntity<Map<String, Object>> shop(
      @RequestParam(required = false) String shopName,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Integer id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int pageSize) {

    int maxItemsShown = 50;

    if (pageSize > maxItemsShown) {
      pageSize = maxItemsShown;
    }

    List<Object> params = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM Shop WHERE 1=1");

    if (shopName != null && !shopName.isEmpty()) {
      sql.append(" AND shopName LIKE ?");
      params.add("%" + shopName + "%");
    }

    if (description != null && !description.isEmpty()) {
      sql.append(" AND description LIKE ?");
      params.add("%" + description + "%");
    }

    if (id != null) {
      sql.append(" AND id = ?");
      params.add(+id);
    }

    sql.append(" LIMIT ?");
    params.add(pageSize);

    sql.append(" OFFSET ?");
    params.add(page * pageSize);

    try {
      List<Map<String, Object>> shops = jdbcTemplate.queryForList(sql.toString(), params.toArray());
      return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", shops));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shop")
  public Map<String, Object> addShop(@RequestBody Shop shop) {
    if (isUserShopOwner()) {
      shop.setShopOwnerid(
          Integer.parseInt(
              (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
    } else if (!isUserAdmin()) {
      return Map.of("error", "You do not have permission to create shops");
    }

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

  @GetMapping("/shop/{id}")
  public ResponseEntity<?> getShop(@PathVariable int id) {
    String sql = "SELECT * FROM `Shop` WHERE id = ?";
    try {
      Shop shop = jdbcTemplate.queryForObject(sql, new ShopRowMapper(), id);
      return ResponseEntity.status(HttpStatus.OK).body(shop);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shopItem/{id}")
  public ResponseEntity<Map<String, Object>> shopItemById(@PathVariable("id") int id) {
    try {
      ShopItem shopItem =
          jdbcTemplate.queryForObject(
              "SELECT * FROM `ShopItem` WHERE id = ?", new ShopItemRowMapper(), id);

      return ResponseEntity.ok(Map.of("success", shopItem));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/shopItem")
  public ResponseEntity<Map<String, Object>> shopItem(
      @RequestParam(required = false) String itemName,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int pageSize) {

    // Set maxItemsShown to limit the max number of items shown
    int maxItemsShown = 50;

    // Ensure pageSize does not exceed the maxItemsShown
    if (pageSize > maxItemsShown) {
      pageSize = maxItemsShown;
    }

    List<Object> params = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM ShopItem WHERE 1=1");

    // Add filters for item name, description, and price
    if (itemName != null && !itemName.isEmpty()) {
      sql.append(" AND itemName LIKE ?");
      params.add("%" + itemName + "%");
    }

    if (description != null && !description.isEmpty()) {
      sql.append(" AND description LIKE ?");
      params.add("%" + description + "%");
    }

    if (minPrice != null && maxPrice == null) {
      sql.append(" AND price >= ?");
      params.add(minPrice);
    } else if (minPrice == null && maxPrice != null) {
      sql.append(" AND price <= ?");
      params.add(maxPrice);
    } else if (minPrice != null && maxPrice != null) {
      sql.append(" AND price BETWEEN ? AND ?");
      params.add(minPrice);
      params.add(maxPrice);
    }

    // Add LIMIT clause to restrict results to maxItemsShown (5 items max)
    sql.append(" LIMIT ?");
    params.add(pageSize);

    // Add OFFSET clause for pagination
    sql.append(" OFFSET ?");
    params.add(page * pageSize);

    try {
      // Execute the query
      List<Map<String, Object>> shopItems =
          jdbcTemplate.queryForList(sql.toString(), params.toArray());
      return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", shopItems));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/shopItem")
  public Map<String, Object> addShopItem(@RequestBody ShopItem shopItem) {
    int shopOwnerId = getShopOwnerIdByShopItem(shopItem);
    if (!isUserAdmin() && !isUserOwnerOfShop(shopOwnerId)) {
      return Map.of("error", "You do not have permission to add shop items");
    }

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
    int shopOwnerId = getShopOwnerIdByShopItemId(id);
    if (!isUserAdmin() && !isUserOwnerOfShop(shopOwnerId)) {
      return Map.of("error", "You do not have permission to edit this shop");
    }

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
    int shopOwnerId = getShopOwnerIdFromShopId(id);
    if (!isUserAdmin() && !isUserOwnerOfShop(shopOwnerId)) {
      return Map.of("error", "You do not have permission to edit this shop");
    }

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
    int shopOwnerId = getShopOwnerIdFromShopId(id);
    if (!isUserAdmin() && !isUserOwnerOfShop(shopOwnerId)) {
      return Map.of("error", "You do not have permission to delete this shop");
    }

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
    int shopOwnerId = getShopOwnerIdByShopItemId(id);
    if (!isUserAdmin() && !isUserOwnerOfShop(shopOwnerId)) {
      return Map.of("error", "You do not have permission to delete this shop item");
    }

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

  protected boolean isUserShopOwner() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    return role.equalsIgnoreCase("ROLE_shopowner");
  }

  protected boolean isUserOwnerOfShop(int id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int userId = Integer.parseInt((String) authentication.getPrincipal());
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    return role.equalsIgnoreCase("ROLE_shopowner") && userId == id;
  }

  protected int getShopOwnerIdByShopItem(ShopItem shopItem) {
    ResponseEntity<?> response = getShop(shopItem.getShopid());
    if (response.getStatusCode() == HttpStatus.OK) {
      Shop shop = (Shop) response.getBody();
      if (shop != null) {
        return shop.getShopOwnerid();
      }
    }
    return -1;
  }

  protected int getShopOwnerIdByShopItemId(int id) {
    ShopItem shopItem = null;
    ResponseEntity<?> response = shopItemById(id);
    if (response.getStatusCode() == HttpStatus.OK) {
      Map<String, Object> map = (Map) response.getBody();
      shopItem = (ShopItem) map.get("success");
    }
    return getShopOwnerIdByShopItem(shopItem);
  }

  protected int getShopOwnerIdFromShopId(int id) {
    ResponseEntity<?> response = getShop(id);
    if (response.getStatusCode() == HttpStatus.OK) {
      Shop s = (Shop) response.getBody();
      if (s != null) {
        return s.getShopOwnerid();
      }
    }

    return -1;
  }

  protected boolean isUserAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    return role.equalsIgnoreCase("ROLE_admin");
  }
}
