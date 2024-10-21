package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class UserServiceTests {
  @Autowired private UserService userService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setup() {
    jdbcTemplate.execute("DROP TABLE IF EXISTS `User`;");
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS `User` (\n"
            + "    id INT AUTO_INCREMENT PRIMARY KEY,\n"
            + "    userType ENUM('admin', 'shopowner', 'customer') NOT NULL,\n"
            + "    username VARCHAR(255) NOT NULL,\n"
            + "    email VARCHAR(255) NOT NULL,\n"
            + "    address VARCHAR(255),\n"
            + "    isHidden BOOLEAN DEFAULT FALSE);");

    jdbcTemplate.execute(
        "INSERT INTO `User` (userType, username, email, address, isHidden) "
            + "VALUES ('shopowner', 'test1', 'test1@email.com', '11,11St',false)");
    jdbcTemplate.execute(
        "INSERT INTO `User` (userType, username, email, address, isHidden) "
            + "VALUES ('customer', 'test2', 'test2@email.com', '22,22St',true)");
    jdbcTemplate.execute(
        "INSERT INTO `User` (userType, username, email, address, isHidden) "
            + "VALUES ('admin', 'test3', 'test3@email.com', '33,33St',false)");
  }

  @Test
  void contextLoads() {}

  @SuppressWarnings({"null", "unchecked"})
  @Test
  void testGetUsers() {

    // get all users
    ResponseEntity<Map<String, Object>> response = userService.users(null);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> responseBody = response.getBody();
    assertTrue(responseBody.containsKey("success"));
    List<User> users = (List<User>) responseBody.get("success");
    assertEquals(3, users.size());

    // get hidden users
    Map<String, String> requestParams = new HashMap<>();
    requestParams.put("isHidden", "true");
    response = userService.users(requestParams);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    responseBody = response.getBody();
    assertTrue(responseBody.containsKey("success"));
    List<User> usersH = (List<User>) responseBody.get("success");
    assertEquals(1, usersH.size());

    // get shown users
    requestParams = new HashMap<>();
    requestParams.put("isHidden", "false");
    response = userService.users(requestParams);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    responseBody = response.getBody();
    assertTrue(responseBody.containsKey("success"));
    List<User> usersS = (List<User>) responseBody.get("success");
    assertEquals(2, usersS.size());
  }
}
