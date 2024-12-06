package CS4337.Project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.Transaction;
import CS4337.Project.Shared.Security.AuthUtils;
import CS4337.project.Order;
import CS4337.project.OrderMapper;
import CS4337.project.OrderService;
import CS4337.project.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private OrderService orderService;

  private Order testOrder;
  private Transaction testTransaction;

  @BeforeEach
  void setUp() {
    testOrder = new Order(1, OrderStatus.PROCESSING, "Test Address", 1, 1, 10.0, 1);

    testTransaction = new Transaction();
    testTransaction.setId(1);
    testTransaction.setSourceUserId(1);
    testTransaction.setAmount(10.0);
  }

  @Test
  void addOrder_Success() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(false);

      when(restTemplate.postForEntity(
              anyString(), any(TransactionRequest.class), eq(Transaction.class)))
          .thenReturn(ResponseEntity.ok(testTransaction));

      when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

      ResponseEntity<?> response = orderService.addOrder(testOrder);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.getBody();
      assertEquals(1, body.get("success"));
    }
  }

  @Test
  void getOrder_Success() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenReturn(testOrder);

      ResponseEntity<?> response = orderService.getOrder(1);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(testOrder, response.getBody());
    }
  }

  @Test
  void getOrder_NotFound() {
    when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(999)))
        .thenReturn(null);

    ResponseEntity<?> response = orderService.getOrder(999);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Order not found", response.getBody());
  }

  @Test
  void updateOrderStatus_Success() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      Order mockOrder = new Order(1, OrderStatus.PROCESSING, "Test Address", 1, 1, 10.0, 1);
      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenReturn(mockOrder);

      when(jdbcTemplate.update(anyString(), eq("DELIVERED"), eq(1))).thenReturn(1);

      ResponseEntity<Map<String, Object>> response = orderService.updateOrderStatus(1, "DELIVERED");

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(1, response.getBody().get("success"));

      verify(jdbcTemplate).update(anyString(), eq("DELIVERED"), eq(1));
    }
  }

  @Test
  void getOrder_Unauthorized() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(2);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(false);

      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenReturn(testOrder);

      ResponseEntity<?> response = orderService.getOrder(1);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertEquals("You do not have permission to view this order", response.getBody());
    }
  }

  @Test
  void addOrder_Unauthorized() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(2);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(false);

      ResponseEntity<?> response = orderService.addOrder(testOrder);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertEquals("You do not have permission to create this order", response.getBody());
    }
  }

  @Test
  void getOrders_EmptyList() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);
      when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
          .thenReturn(new ArrayList<>());

      ResponseEntity<Map<String, Object>> response = orderService.getOrders(0, 10);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> orders =
          (List<Map<String, Object>>) response.getBody().get("orders");
      assertTrue(orders.isEmpty());
    }
  }

  @Test
  void updateOrderStatus_OrderNotFound() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(999)))
          .thenThrow(new EmptyResultDataAccessException(1));

      when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(0);

      ResponseEntity<Map<String, Object>> response =
          orderService.updateOrderStatus(999, "DELIVERED");

      assertNotNull(response);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertEquals("Order not found", response.getBody().get("error"));

      verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }
  }

  @Test
  void addOrder_PaymentValidationFailed() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(false);

      when(restTemplate.postForEntity(
              anyString(), any(TransactionRequest.class), eq(Transaction.class)))
          .thenReturn(ResponseEntity.badRequest().body(null));

      ResponseEntity<?> response = orderService.addOrder(testOrder);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(((String) response.getBody()).contains("Payment validation failed"));
    }
  }

  @Test
  void getOrders_DatabaseError() {
    lenient()
        .when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
        .thenThrow(new RuntimeException("Database error"));

    ResponseEntity<Map<String, Object>> response = orderService.getOrders(0, 10);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertTrue(((String) response.getBody().get("error")).contains("Failed to retrieve orders"));
  }

  @Test
  void updateOrderStatus_InvalidStatus() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenReturn(testOrder);

      when(jdbcTemplate.update(anyString(), anyString(), anyInt()))
          .thenThrow(new RuntimeException("Invalid status"));

      ResponseEntity<Map<String, Object>> response =
          orderService.updateOrderStatus(1, "INVALID_STATUS");

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertTrue(
          ((String) response.getBody().get("error")).contains("Failed to update order status"));
    }
  }

  @Test
  void getOrder_InvalidId() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      ResponseEntity<?> response = orderService.getOrder(-1);

      assertNotNull(response);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertEquals("Order not found", response.getBody());
    }
  }

  @Test
  void addOrder_NullOrder() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      ResponseEntity<?> response = orderService.addOrder(null);

      assertNotNull(response);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
  }

  @Test
  void updateOrderStatus_NullStatus() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);
      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenReturn(testOrder);

      ResponseEntity<Map<String, Object>> response = orderService.updateOrderStatus(1, null);

      assertNotNull(response);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertEquals("Order not found", response.getBody().get("error"));
    }
  }

  @Test
  void updateOrderStatus_EmptyDatabase() {
    try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
      authUtils.when(AuthUtils::getUserId).thenReturn(1);
      authUtils.when(AuthUtils::isUserAdmin).thenReturn(true);

      when(jdbcTemplate.queryForObject(anyString(), any(OrderMapper.class), eq(1)))
          .thenThrow(new EmptyResultDataAccessException(1));

      ResponseEntity<Map<String, Object>> response = orderService.updateOrderStatus(1, "DELIVERED");

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertEquals("Order not found", response.getBody().get("error"));
    }
  }
}
