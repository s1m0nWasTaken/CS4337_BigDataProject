package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  }

  @Test
  public void testGetAllUsers() {
    when(jdbcTemplate.query(anyString(), any(UserRowMapper.class))).thenReturn(List.of(testUser));

    ResponseEntity<Map<String, Object>> response = userService.users(null);

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
}
