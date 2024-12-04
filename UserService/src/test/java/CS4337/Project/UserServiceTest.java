package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import CS4337.Project.Shared.Models.User;
import CS4337.Project.Shared.Models.UserType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private UserService userService;

  private User testUser;

  @BeforeEach
  public void setup() {
    testUser = new User(UserType.customer, "johndoe", "john@example.com", "123 Main St");
    // lenient so it will still make the stub even though it isnt used in every test
    UserService spyUserService = Mockito.spy(userService);
    lenient().doReturn(true).when(spyUserService).isUserAuthorized(anyInt());
    userService = spyUserService;
  }

  @Test
  public void testGetAllUsers() {
    when(jdbcTemplate.query(anyString(), any(UserRowMapper.class))).thenReturn(List.of(testUser));

    ResponseEntity<Map<String, Object>> response = userService.users(null, 0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of(testUser), response.getBody().get("success"));
  }

  @Test
  public void testAddUser() {
    when(jdbcTemplate.update(
            anyString(),
            eq(testUser.getUserType().name()),
            eq(testUser.getUsername()),
            eq(testUser.getEmail()),
            eq(testUser.getAddress()),
            eq(false)))
        .thenReturn(1);

    ResponseEntity<Map<String, Object>> response = userService.addUser(testUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
  }

  @Test
  public void testGetUserById() {
    when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(1)))
        .thenReturn(testUser);

    ResponseEntity<Map<String, Object>> response = userService.getUser(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(testUser, response.getBody().get("success"));
  }

  @Test
  public void testGetUserByEmail() {
    when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("john@example.com")))
        .thenReturn(testUser);

    ResponseEntity<Map<String, Object>> response = userService.getUserByEmail("john@example.com");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(testUser, response.getBody().get("success"));
  }

  @Test
  public void testDeleteUser() {
    when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

    ResponseEntity<Map<String, String>> response = userService.deleteUser(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("User deleted successfully", response.getBody().get("message"));
  }

  @Test
  public void testUpdateUser() {
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = userService.updateUser(1, testUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("1", response.getBody().get("success"));
  }

  @Test
  public void testBanUser() {
    when(jdbcTemplate.update(anyString(), eq(true), eq(1))).thenReturn(1);

    ResponseEntity<Map<String, Object>> response =
        userService.banUser(1, Map.of("isHidden", "true"));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("User hidden"));
  }

  @Test
  public void testAddUserMissingUsername() {
    User invalidUser = new User(UserType.customer, "", "john@example.com", "123 Main St");

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq(""),
            eq("john@example.com"),
            eq("123 Main St"),
            eq(false)))
        .thenReturn(1);

    ResponseEntity<Map<String, Object>> response = userService.addUser(invalidUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
  }

  @Test
  public void testAddUserDuplicateEmail() {
    User duplicateUser = new User(UserType.customer, "johndoe", "john@example.com", "123 Main St");

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq("johndoe"),
            eq("john@example.com"),
            eq("123 Main St"),
            eq(false)))
        .thenThrow(new DataAccessException("Duplicate email") {});

    ResponseEntity<Map<String, Object>> response = userService.addUser(duplicateUser);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Duplicate email", response.getBody().get("error"));
  }

  @Test
  public void testAddUserWithMissingAddress() {
    User userWithMissingAddress = new User(UserType.customer, "johndoe", "john@example.com", null);

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq("johndoe"),
            eq("john@example.com"),
            eq(null),
            eq(false)))
        .thenThrow(new DataAccessException("Address is required") {});

    ResponseEntity<Map<String, Object>> response = userService.addUser(userWithMissingAddress);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Address is required", response.getBody().get("error"));
  }

  @Test
  public void testAddSpecialCharactersInUsername() {
    User specialCharUser = new User(UserType.customer, "us$#@!", "john@example.com", "123 Main St");

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq("us$#@!"),
            eq("john@example.com"),
            eq("123 Main St"),
            eq(false)))
        .thenReturn(1);

    ResponseEntity<Map<String, Object>> response = userService.addUser(specialCharUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
  }

  @Test
  public void testAddUserWithLongAddress() {
    String longAddress = "A".repeat(1000);
    User user = new User(UserType.customer, "johndoe", "john@example.com", longAddress);

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq("johndoe"),
            eq("john@example.com"),
            eq(longAddress),
            eq(false)))
        .thenThrow(new DataAccessException("Address too long") {});

    ResponseEntity<Map<String, Object>> response = userService.addUser(user);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Address too long", response.getBody().get("error"));
  }

  @Test
  public void testAddUserDuplicateUsername() {
    User duplicateUser =
        new User(UserType.customer, "duplicateuser", "john@example.com", "123 Main St");

    when(jdbcTemplate.update(
            anyString(),
            eq(UserType.customer.name()),
            eq("duplicateuser"),
            eq("john@example.com"),
            eq("123 Main St"),
            eq(false)))
        .thenThrow(new DataAccessException("Duplicate username") {});

    ResponseEntity<Map<String, Object>> response = userService.addUser(duplicateUser);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Duplicate username", response.getBody().get("error"));
  }

  @Test
  public void testGetUsersWithNullFilter() {
    List<User> allUsers =
        List.of(
            new User(1, "customer", "john1", "john1@example.com", "123 Main St", false),
            new User(2, "admin", "john2", "john2@example.com", "1234 Main St", true));

    when(jdbcTemplate.query(eq("SELECT * FROM User"), any(UserRowMapper.class)))
        .thenReturn(allUsers);

    ResponseEntity<Map<String, Object>> response = userService.users(null, 0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allUsers, response.getBody().get("success"));
  }

  @Test
  public void testGetHiddenUsersOnly() {
    List<User> hiddenUsers =
        List.of(new User(1, "customer", "hiddenuser", "john@example.com", "Unknown", true));

    when(jdbcTemplate.query(
            eq("SELECT * FROM User WHERE isHidden = ? AND id > ? ORDER BY id LIMIT ?"),
            any(UserRowMapper.class),
            eq(true),
            eq(0),
            eq(50)))
        .thenReturn(hiddenUsers);

    ResponseEntity<Map<String, Object>> response = userService.users(true, 0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(hiddenUsers, response.getBody().get("success"));
  }

  @Test
  public void testGetUserByEmptyEmail() {
    when(jdbcTemplate.queryForObject(
            eq("SELECT * FROM User WHERE email = ?"), any(UserRowMapper.class), eq("")))
        .thenThrow(new DataAccessException("User not found") {});

    ResponseEntity<Map<String, Object>> response = userService.getUserByEmail("");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("User not found", response.getBody().get("error"));
  }

  @Test
  public void testUpdateUserToDuplicateEmail() {
    User updates = new User();
    updates.setEmail("duplicate@example.com");

    when(jdbcTemplate.update(anyString(), eq("duplicate@example.com"), eq(1)))
        .thenThrow(new DataAccessException("Email already in use") {});

    ResponseEntity<Map<String, Object>> response = userService.updateUser(1, updates);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Email already in use", response.getBody().get("error"));
  }

  @Test
  public void testUpdateUserWithNullFields() {
    int userId = 1;
    User updates = new User();

    when(jdbcTemplate.update(anyString(), eq(userId))).thenReturn(0);

    ResponseEntity<Map<String, Object>> response = userService.updateUser(userId, updates);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("0", response.getBody().get("success"));
  }

  @Test
  public void testDeleteNonExistentUser() {
    when(jdbcTemplate.update(eq("DELETE FROM User WHERE id = ?"), eq(999))).thenReturn(0);

    ResponseEntity<Map<String, String>> response = userService.deleteUser(999);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("User deleted successfully", response.getBody().get("message"));
  }

  @Test
  public void testDeleteUserNonNumericID() {
    when(jdbcTemplate.update(eq("DELETE FROM User WHERE id = ?"), eq(0)))
        .thenThrow(new DataAccessException("Invalid ID") {});

    ResponseEntity<Map<String, String>> response = userService.deleteUser(0);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid ID", response.getBody().get("error"));
  }

  @Test
  public void testBanUserAlreadyBanned() {
    Map<String, String> requestBody = Map.of("isHidden", "true");

    when(jdbcTemplate.update(anyString(), eq(true), eq(1))).thenReturn(0);

    ResponseEntity<Map<String, Object>> response = userService.banUser(1, requestBody);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("bad request", response.getBody().get("error"));
  }

  @Test
  public void testBanUserWithoutId() {
    Map<String, String> requestBody = Map.of("isHidden", "true");

    when(jdbcTemplate.update(anyString(), eq(true), eq(0)))
        .thenThrow(new DataAccessException("Invalid user ID") {});

    ResponseEntity<Map<String, Object>> response = userService.banUser(0, requestBody);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid user ID", response.getBody().get("error"));
  }

  @Test
  public void testGetUsersWithInvalidQuery() {
    when(jdbcTemplate.query(anyString(), any(UserRowMapper.class)))
        .thenThrow(new DataAccessException("Invalid query") {});

    ResponseEntity<Map<String, Object>> response = userService.users(null, 0, 50);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid query", response.getBody().get("error"));
  }
}
