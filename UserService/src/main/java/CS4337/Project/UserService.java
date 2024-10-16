package CS4337.Project;

import jakarta.websocket.server.PathParam;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

  @GetMapping("/user/{id}")
  public Map<String, User> getUser(@PathParam("id") int id) {
    User user = jdbcTemplate.queryForObject("SELECT * FROM User WERE id=${id};", User.class);
    assert user != null;
    return Map.of("user", user);
  }

  @PostMapping("/users")
  public Map<String, Object> addUser(@RequestBody User user) {
    try {
      SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate);
      String sqlInsert =
          "INSERT INTO `User` (userType, username, email, address) " + "VALUES (?, ?, ?, ?)";
      jdbcTemplate.update(
          sqlInsert,
          user.getUserType().name(),
          user.getUsername(),
          user.getEmail(),
          user.getAdress());
      return Map.of("success", 1);
    } catch (TransientDataAccessResourceException e) {
      return Map.of("error", e.getMessage());
    }
  }
}
