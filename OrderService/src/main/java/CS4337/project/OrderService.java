package CS4337.project;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.Transaction;
import CS4337.Project.Shared.Security.AuthUtils;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class OrderService {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired
  @Qualifier("msRestTemplate") private RestTemplate restTemplate;

  private final String PAYMENT_SERVICE_URL = "http://PAYMENTSERVICE";

  public static void main(String[] args) {
    SpringApplication.run(OrderService.class, args);
  }

  @PostMapping("/order")
  public ResponseEntity<?> addOrder(@RequestBody Order order) {
    if (!AuthUtils.isUserAdmin() && AuthUtils.getUserId() != order.getUserId()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("You do not have permission to create this order");
    }

    try {
      // Validate payment before making order
      TransactionRequest transactionRequest = new TransactionRequest();
      transactionRequest.setUserId(order.getUserId());
      transactionRequest.setShopItemId(order.getShopItemid());
      transactionRequest.setQuantity(1); // default as 1 for testing

      ResponseEntity<?> paymentResponse = validatePayment(transactionRequest);
      if (paymentResponse.getStatusCode() != HttpStatus.OK) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Payment validation failed: " + paymentResponse.getBody());
      }
      Transaction t = (Transaction) paymentResponse.getBody();
      String sqlInsert =
          "INSERT INTO `Orders` (orderDate, orderStatus, deliveryAddress, shopItemid, transactionid, price, userid) "
              + "VALUES (?,?,?,?,?,?,?)";

      jdbcTemplate.update(
          sqlInsert,
          new Timestamp(System.currentTimeMillis()),
          "PROCESSING",
          order.getDeliveryAddress(),
          order.getShopItemid(),
          t.getId(),
          t.getAmount(),
          t.getSourceUserId());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("success", 1, "message", "Order placed successfully"));
    } catch (Exception e) {
      System.out.println(e.getClass());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  private ResponseEntity<?> validatePayment(TransactionRequest transactionRequest) {
    try {
      return restTemplate.postForEntity(
          PAYMENT_SERVICE_URL + "/payment/create", transactionRequest, Transaction.class);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error communicating with Payment Service: " + e.getMessage());
    }
  }

  @PutMapping("/order/{orderId}/status")
  public ResponseEntity<Map<String, Object>> updateOrderStatus(
      @PathVariable int orderId, @RequestParam String status) {
    try {
      ResponseEntity<?> response = getOrder(orderId);
      if (response.getStatusCode() != HttpStatus.OK) {
        Map<String, Object> orderData = (Map<String, Object>) response.getBody();
        Order order = (Order) orderData.get("success");

        if (!AuthUtils.isUserAdmin() && AuthUtils.getUserId() != order.getUserId()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("error", "You do not have permission to update this order"));
        }
      }

      String sqlUpdate = "UPDATE Orders SET orderStatus = ? WHERE id = ?";
      int rowsAffected = jdbcTemplate.update(sqlUpdate, status, orderId);

      if (rowsAffected > 0) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(Map.of("success", 1, "message", "Order status updated successfully"));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to update order status: " + e.getMessage()));
    }
  }

  @GetMapping("/orders")
  public ResponseEntity<Map<String, Object>> getOrders(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    try {
      String sqlQuery = "";
      Object[] params = {};

      if (AuthUtils.isUserAdmin()) {
        sqlQuery = "SELECT * FROM Orders LIMIT ? OFFSET ?";
        params = new Object[] {size, page * size};
      } else if (AuthUtils.isUserOwner()) {
        int shopOwnerId = AuthUtils.getUserId();

        String shopIdsQuery = "SELECT id FROM Shop WHERE shopOwnerid = ?";
        List<Integer> shopIds = jdbcTemplate.queryForList(shopIdsQuery, Integer.class, shopOwnerId);

        if (shopIds.isEmpty()) {
          return ResponseEntity.status(HttpStatus.OK)
              .body(Map.of("success", 1, "orders", List.of()));
        }

        sqlQuery =
            "SELECT o.* FROM Orders o "
                + "JOIN ShopItems si ON o.shopItemid = si.id "
                + "WHERE si.shopid IN (%s) LIMIT ? OFFSET ?";
        String inClause =
            String.join(",", shopIds.stream().map(String::valueOf).toArray(String[]::new));
        sqlQuery = String.format(sqlQuery, inClause);

        params = new Object[] {size, page * size};
      } else if (AuthUtils.isUser()) {
        sqlQuery = "SELECT * FROM Orders WHERE userId = ? LIMIT ? OFFSET ?";
        int userId = AuthUtils.getUserId();
        params = new Object[] {userId, size, page * size};
      } else {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You do not have permission to view orders"));
      }

      List<Map<String, Object>> orders = jdbcTemplate.queryForList(sqlQuery, params);

      return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", 1, "orders", orders));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to retrieve orders: " + e.getMessage()));
    }
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<?> getOrder(@PathVariable int orderId) {
    try {
      String sql = "SELECT * FROM Orders WHERE id = ?";
      Order order = jdbcTemplate.queryForObject(sql, new OrderMapper(), orderId);

      if (order == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
      }

      if (!AuthUtils.isUserAdmin() || AuthUtils.getUserId() != order.getUserId()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("You do not have permission to view this order");
      }

      return ResponseEntity.status(HttpStatus.OK).body(order);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "error", "Failed to retrieve order with id " + orderId + ": " + e.getMessage()));
    }
  }
}
