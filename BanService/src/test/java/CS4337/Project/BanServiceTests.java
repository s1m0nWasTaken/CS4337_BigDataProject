package CS4337.Project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import CS4337.Project.Shared.DTO.ReqBan;
import CS4337.Project.Shared.DTO.ShopBan;
import CS4337.Project.Shared.DTO.UserBan;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class BanServiceTests {

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private BanService banService;

  private static final String TEST_USER_SERVICE_URL = "http://test/user/ban/{id}";
  private static final String TEST_SHOP_SERVICE_URL = "http://test/shopItem/ban/{id}";

  private Date testDate;
  private UserBan testUserBan;
  private ShopBan testShopBan;
  private ReqBan testReqBan;

  @BeforeEach
  void setUp() {
    testDate = new Date(System.currentTimeMillis());
    testUserBan = new UserBan(1, 1, testDate, true);
    testShopBan = new ShopBan(1, 1, testDate, true);
    testReqBan = new ReqBan();
    testReqBan.setBannedId(1);
    testReqBan.setSuspendedUntil(testDate);

    ReflectionTestUtils.setField(banService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(
        banService, "shopServiceUrl", "http://SHOPSERVICE/shopItem/ban/{id}");
  }

  @Test
  void checkUserBans_DatabaseError() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), any(Date.class)))
        .thenThrow(new DataAccessException("Database error") {});

    ResponseEntity<Map<String, ?>> response = banService.checkUserBans();

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().containsKey("error"));
  }

  @Test
  void getUserBans_Success() {
    List<UserBan> expectedBans = List.of(testUserBan);
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), anyInt()))
        .thenReturn(expectedBans);

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertEquals(expectedBans, response.getBody().get("success"));
  }

  @Test
  void getShopBans_Success() {
    List<ShopBan> expectedBans = List.of(testShopBan);
    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), anyInt()))
        .thenReturn(expectedBans);

    ResponseEntity<Map<String, Object>> response = banService.getShopBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertEquals(expectedBans, response.getBody().get("success"));
  }

  @Test
  void checkShopBans_NoShopsToUnban() {
    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), any(Date.class)))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, ?>> response = banService.checkShopBans();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("no shop items to unban", response.getBody().get("success"));
  }

  @Test
  void checkShopBans_DatabaseError() {
    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), any(Date.class)))
        .thenThrow(new DataAccessException("Database error") {});

    ResponseEntity<Map<String, ?>> response = banService.checkShopBans();

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().containsKey("error"));
  }

  @Test
  void banShop_InvalidId() {
    testReqBan.setBannedId(0);

    ResponseEntity<Map<String, Object>> response = banService.banShop(testReqBan);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("0 is not a valid bannedId", response.getBody().get("error"));
  }

  @Test
  void getUserBans_EmptyResult() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), anyInt()))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertEquals(new ArrayList<>(), response.getBody().get("success"));
  }

  @Test
  void getShopBans_EmptyResult() {
    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), anyInt()))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, Object>> response = banService.getShopBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertEquals(new ArrayList<>(), response.getBody().get("success"));
  }

  @Test
  void banShop_LargeBannedId() {
    testReqBan.setBannedId(Integer.MAX_VALUE);

    when(jdbcTemplate.update(anyString(), eq(Integer.MAX_VALUE), any(Date.class))).thenReturn(1);

    Map<String, Object> responseBody = Map.of("status", "success");
    ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(responseBody);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            eq(Integer.MAX_VALUE)))
        .thenReturn(mockResponse);

    ResponseEntity<Map<String, Object>> response = banService.banShop(testReqBan);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
  }

  @Test
  void banUser_AlreadyBanned() {
    testReqBan.setBannedId(1);

    Map<String, Object> responseBody = Map.of("error", "already banned");
    ResponseEntity<Map<String, Object>> mockResponse =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            eq(1)))
        .thenReturn(mockResponse);

    ResponseEntity<Map<String, Object>> response = banService.banUser(testReqBan);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("already banned", response.getBody().get("error"));
  }

  @Test
  void banShop_EmptyRequest() {
    ReqBan emptyRequest = new ReqBan();

    ResponseEntity<Map<String, Object>> response = banService.banShop(emptyRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("0 is not a valid bannedId", response.getBody().get("error"));
  }

  @Test
  void getShopBans_NullResult() {

    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), anyInt()))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, Object>> response = banService.getShopBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertTrue(((List<?>) response.getBody().get("success")).isEmpty());
  }

  @Test
  void banUser_NullSuspensionDate() {
    testReqBan.setBannedId(1);
    testReqBan.setSuspendedUntil(null);

    assertThrows(
        AssertionError.class,
        () -> {
          banService.banUser(testReqBan);
        });
  }

  @Test
  void getUserBans_DatabaseConnectionFailure() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), anyInt()))
        .thenThrow(new DataAccessException("Database connection failed") {});

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(1);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().containsKey("error"));
    assertEquals("Database connection failed", response.getBody().get("error"));
  }

  @Test
  void getUserBans_InvalidInput() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), eq(-1)))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(-1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertTrue(((List<?>) response.getBody().get("success")).isEmpty());
  }

  @Test
  void banShop_NoDatabaseInteraction() {
    testReqBan.setBannedId(1);
    testReqBan.setSuspendedUntil(new Date(System.currentTimeMillis()));

    ResponseEntity<Map<String, Object>> mockResponse =
        ResponseEntity.badRequest().body(Map.of("error", "shop not found"));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            eq(1)))
        .thenReturn(mockResponse);

    ResponseEntity<Map<String, Object>> response = banService.banShop(testReqBan);

    verify(jdbcTemplate, never()).update(anyString(), anyInt(), any(Date.class));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("shop not found", response.getBody().get("error"));
  }

  @Test
  void banUser_EmptyReqBan() {
    ReqBan emptyReqBan = new ReqBan();

    ResponseEntity<Map<String, Object>> response = banService.banUser(emptyReqBan);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("0 is not a valid bannedId", response.getBody().get("error"));
  }

  @Test
  void getUserBans_MultipleBans() {
    List<UserBan> multipleBans =
        List.of(new UserBan(1, 1, testDate, true), new UserBan(2, 1, testDate, false));
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), eq(1))).thenReturn(multipleBans);

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertEquals(multipleBans, response.getBody().get("success"));
  }

  @Test
  void getUserBans_NullDatabaseResult() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), eq(1)))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, Object>> response = banService.getUserBans(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("success"));
    assertTrue(((List<?>) response.getBody().get("success")).isEmpty());
  }

  @Test
  void checkUserBans_EmptyDatabase() {
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), any(Date.class)))
        .thenReturn(new ArrayList<>());

    ResponseEntity<Map<String, ?>> response = banService.checkUserBans();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("no users to unban", response.getBody().get("success"));
  }

  @Test
  void checkUserBans_InvalidRestResponse() {
    List<UserBan> userBans = List.of(testUserBan);
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), any(Date.class)))
        .thenReturn(userBans);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            anyInt()))
        .thenReturn(ResponseEntity.badRequest().body(Map.of("error", "invalid request")));

    ResponseEntity<Map<String, ?>> response = banService.checkUserBans();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("unbanned"));
    assertTrue(((List<?>) response.getBody().get("unbanned")).isEmpty());
  }

  @Test
  void checkUserBans_LargeDataset() {
    List<UserBan> userBans = new ArrayList<>();
    for (int i = 1; i <= 1000; i++) {
      userBans.add(new UserBan(i, i, testDate, true));
    }
    when(jdbcTemplate.query(anyString(), any(UserBanMapper.class), any(Date.class)))
        .thenReturn(userBans);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            anyInt()))
        .thenReturn(ResponseEntity.ok(Map.of("status", "success")));

    when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1);

    ResponseEntity<Map<String, ?>> response = banService.checkUserBans();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("unbanned"));
    assertEquals(1000, ((List<?>) response.getBody().get("unbanned")).size());
  }

  @Test
  void banUser_NoDatabaseUpdate() {
    testReqBan.setBannedId(1);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            eq(1)))
        .thenReturn(ResponseEntity.badRequest().body(Map.of("error", "user not found")));

    ResponseEntity<Map<String, Object>> response = banService.banUser(testReqBan);

    verify(jdbcTemplate, never()).update(anyString(), anyInt(), any(Date.class));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("user not found", response.getBody().get("error"));
  }

  @Test
  void banUser_UnexpectedException() {
    testReqBan.setBannedId(1);

    when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            anyInt()))
        .thenThrow(new RuntimeException("Unexpected error"));

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              banService.banUser(testReqBan);
            });

    assertEquals("Unexpected error", exception.getMessage());
  }

  @Test
  void checkShopBans_InvalidRestResponse() {
    List<ShopBan> shopBans = List.of(testShopBan);
    when(jdbcTemplate.query(anyString(), any(ShopBanMapper.class), any(Date.class)))
        .thenReturn(shopBans);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any(),
            anyInt()))
        .thenReturn(ResponseEntity.badRequest().body(Map.of("error", "invalid request")));

    ResponseEntity<Map<String, ?>> response = banService.checkShopBans();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("unbanned"));
    assertTrue(((List<?>) response.getBody().get("unbanned")).isEmpty());
  }
}
