package CS4337.project;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.Transaction;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired private RestTemplate restTemplate;

  private final String PAYMENT_SERVICE_URL = "http://PAYMENTSERVICE";

  public static void main(String[] args) {
    SpringApplication.run(OrderService.class, args);
  }

  @PostMapping("/order")
  public ResponseEntity<?> addOrder(@RequestBody Order order) {
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
      String sqlQuery = "SELECT * FROM Orders LIMIT ? OFFSET ?";
      Object[] params = {size, page * size};

      List<Map<String, Object>> orders = jdbcTemplate.queryForList(sqlQuery, params);

      return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", 1, "orders", orders));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to retrieve orders: " + e.getMessage()));
    }
  }
}
