package CS4337.Project;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class UserService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(UserService.class, args);
  }

  @GetMapping("/users") // pass an ishidden feild in json body to choose
  public ResponseEntity<Map<String, Object>> users(
      @RequestParam(required = false) Boolean isHidden) {
    // TODO: later add a check with auth if admin and if so allow to show hidden
    // and normal else only normal
    try {
      if (isHidden != null) {
        boolean hidden = isHidden;
        List<User> users =
            jdbcTemplate.query(
                "SELECT * FROM User WHERE isHidden = ?", new UserRowMapper(), hidden);

        return ResponseEntity.ok(Map.of("success", users));
      } else {
        List<User> users = jdbcTemplate.query("SELECT * FROM User", new UserRowMapper());

        return ResponseEntity.ok(Map.of("success", users));
      }
    } catch (DataAccessException e) {

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/users")
  public ResponseEntity<Map<String, Object>> addUser(@RequestBody User user) {
    try {
      String sqlInsert =
          "INSERT INTO `User` (userType, username, email, address, isHidden) "
              + "VALUES (?, ?, ?, ?,?)";
      jdbcTemplate.update(
          sqlInsert,
          user.getUserType().name(),
          user.getUsername(),
          user.getEmail(),
          user.getAddress(),
          user.getIsHidden());
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

      return ResponseEntity.ok(Map.of("success", user));
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
      if (userUpdates.getAddress() != null) {
        updateStatement.append("address = ?, ");
        params.add(userUpdates.getAddress());
      }

      updateStatement.setLength(updateStatement.length() - 2); // Remove last ", "
      updateStatement.append(" WHERE id = ?");
      params.add(id);

      int updatedRows = jdbcTemplate.update(updateStatement.toString(), params.toArray());
      return ResponseEntity.ok(Map.of("success", String.valueOf(updatedRows)));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("user/ban/{id}")
  public ResponseEntity<Map<String, Object>> banUser(
      @PathVariable("id") int id, @RequestBody Map<String, String> requestBody) {
    try {
      String hiddenStr = requestBody.get("isHidden");
      Boolean hidden = (hiddenStr.equals("true") ? true : false);
      String banStatement = "UPDATE `User` SET isHidden = ? WHERE id = ?";
      int sucess = jdbcTemplate.update(banStatement, hidden, id);
      if (sucess == 1) {
        return ResponseEntity.ok(Map.of("User hidden", hidden));
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
  }
}
