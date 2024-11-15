package CS4337.Project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserService userService;

    @Test
    public void testGetAllUsers() {
        
        List<User> users = Arrays.asList(
            new User(1, "admin", "AdminUser", "admin@example.com", "Admin Address", false),
            new User(2, "customer", "CustomerUser", "customer@example.com", "Customer Address", false)
        );

        when(jdbcTemplate.query(anyString(), any(UserRowMapper.class))).thenReturn(users);

        
        ResponseEntity<Map<String, Object>> response = userService.users(null);

        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody().get("success"));
        //Test to verify if the UserService retrieves all users from the database.
        //This test ensures that when no filter is applied, the service correctly fetches all users.
    }

    @Test
public void testAddUser() {
    
    User newUser = new User(UserType.customer, "NewUser", "newuser@example.com", "New Address");
    when(jdbcTemplate.update(
        anyString(),
        eq(UserType.customer.name()),
        eq("NewUser"),
        eq("newuser@example.com"),
        eq("New Address"),
        eq(false)
    )).thenReturn(1);

    
    ResponseEntity<Map<String, Object>> response = userService.addUser(newUser);

    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
    //Test to verify if the UserService successfully adds a new user.
    //This test simulates adding a user to the database and checks for a successful response.
}


@Test
public void testUpdateUser() {
    
    int userId = 1;
    User updates = new User();
    updates.setUsername("UpdatedUser");
    updates.setEmail("updateduser@example.com");

    when(jdbcTemplate.update(
        anyString(),
        eq("UpdatedUser"),
        eq("updateduser@example.com"),
        eq(userId)
    )).thenReturn(1);

    
    ResponseEntity<Map<String, Object>> response = userService.updateUser(userId, updates);

    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("1", response.getBody().get("success"));
    //Test to verify if the UserService correctly updates an existing user's details.
    //This test ensures that the update operation modifies the username and email fields as expected.
}


    @Test
    public void testBanUser() {
        int userId = 1;
        Map<String, String> requestBody = Map.of("isHidden", "true");

        when(jdbcTemplate.update(anyString(), eq(true), eq(userId))).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = userService.banUser(userId, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("User hidden"));
        //Test to verify if the UserService successfully bans (hides) a user.
        //This test checks that the user is marked as hidden in the database with the correct flag.
    }

    @Test
    public void testGetAllUsersWithHiddenFilter() {
        List<User> users = Arrays.asList(
            new User(1, "admin", "AdminUser", "admin@example.com", "Admin Address", true)
        );

        when(jdbcTemplate.query(anyString(), any(UserRowMapper.class), eq(true))).thenReturn(users);

        ResponseEntity<Map<String, Object>> response = userService.users(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody().get("success"));
        //Test to verify if the UserService retrieves only hidden users when the isHidden filter is applied.
        //This test ensures that the service respects the filter and fetches the appropriate users.
    }
}
