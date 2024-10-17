package CS4337.Project;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class UserService {

  @Autowired private JdbcTemplate jdbcTemplate;

  // @Autowired private SimpleJdbcInsert insert;

  public static void main(String[] args) {
    SpringApplication.run(UserService.class, args);
  }

  @GetMapping("/")
  public Map<String, Object> sayhello() {
    return Map.of("message", "Hello World");
  }

  @GetMapping("/users")
  public List<Map<String, Object>> users() {
    List<Map<String, Object>> users;
    try {
      users = jdbcTemplate.queryForList("SELECT * FROM User");
    } catch (DataAccessException e) {
      jdbcTemplate.execute("USE user_service;");
      jdbcTemplate.execute(
          "CREATE TABLE `User` ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "userType ENUM('admin', 'shopowner', 'customer') NOT NULL, "
              + "username VARCHAR(255) NOT NULL, "
              + "email VARCHAR(255) NOT NULL, "
              + "address VARCHAR(255), "
              + "suspendedUntil DATETIME);");
      users = jdbcTemplate.queryForList(" SELECT *FROM User ");
    }

    return users;
  }

  @PostMapping("/users")
  public ResponseEntity<Map<String, Object>> addUser(@RequestBody User user) {
    try {
      String sqlInsert =
          "INSERT INTO `User` (userType, username, email, address, suspendedUntil) "
              + "VALUES (?, ?, ?, ?,?)";
      jdbcTemplate.update(
          sqlInsert,
          user.getUserType().name(),
          user.getUsername(),
          user.getEmail(),
          user.getAdress(),
          Date.valueOf("1000-01-01"));
      return ResponseEntity.ok(Map.of("success", 1));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<Map<String, Object>> getUser(@PathVariable("id") int id) {
    try {
      User user =
          jdbcTemplate.queryForObject("SELECT * FROM User WHERE id = ?", new UserRowMapper(), id);

      return ResponseEntity.ok(Map.of("sucess", user));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/user/{id}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable("id") int id) {
    try {
      jdbcTemplate.update("DELETE FROM User WHERE id = ?", id);

      return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("user/{id}")
  public ResponseEntity<Map<String, Object>> updateUser(
      @PathVariable("id") int id, @RequestBody User userUpdates) {
    try {
      List<Object> params = new ArrayList<>();
      StringBuilder updateStatement = new StringBuilder("UPDATE User SET ");

      // only update feilds that are supplied in the request
      if (userUpdates.getUserType() != null) {
        updateStatement.append("userType = ?, ");
        params.add(userUpdates.getUserType().name());
      }
      if (userUpdates.getUsername() != null) {
        updateStatement.append("username = ?, ");
        params.add(userUpdates.getUsername());
      }
      if (userUpdates.getEmail() != null) {
        updateStatement.append("email = ?, ");
        params.add(userUpdates.getEmail());
      }
      if (userUpdates.getAdress() != null) {
        updateStatement.append("address = ?, ");
        params.add(userUpdates.getAdress());
      }

      updateStatement.setLength(updateStatement.length() - 2); // Remove last ", "
      updateStatement.append(" WHERE id = ?");
      params.add(id);

      int updatedRows = jdbcTemplate.update(updateStatement.toString(), params.toArray());
      return ResponseEntity.ok(Map.of("sucess", String.valueOf(updatedRows)));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("user/ban/{id}")
  public ResponseEntity<Map<String, Object>> banUser(
      @PathVariable("id") int id, @RequestBody Map<String, String> requestBody) {
    try {
      String dateStr = requestBody.get("date");
      Date date = Date.valueOf(dateStr);
      String banStatement = "UPDATE `User` SET suspendedUntil = ? WHERE id = ?";
      jdbcTemplate.update(banStatement, date, id);
      return ResponseEntity.ok(Map.of("User banned until", date));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }
}
