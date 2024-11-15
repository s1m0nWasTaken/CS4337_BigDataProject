package CS4337.project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class OrderService {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(OrderService.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> addOrder(@RequestBody Order order) {
        try {
            // Check if the shopItem exists via SHOP-SERVICE
            String shopItemServiceUrl = "http://SHOP-SERVICE/shopItem?id=" + order.getShopItemid();
            Map shopItemResponse = restTemplate.getForObject(shopItemServiceUrl, Map.class);

            if (shopItemResponse != null && shopItemResponse.containsKey("success")) {
                String sqlInsert =
                        "INSERT INTO Orders (orderDate, orderStatus, deliveryAddress, shopItemid, transactionid, price) "
                                + "VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        sqlInsert,
                        LocalDateTime.now(),
                        "PENDING",
                        order.getDeliveryAddress(),
                        order.getShopItemid(),
                        order.getTransactionid(),
                        order.getPrice());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("success", 1, "message", "Order placed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid ShopItem ID"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to place order: " + e.getMessage()));
        }
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable int orderId, @RequestParam String status) {

        try {
            String sqlUpdate = "UPDATE Orders SET orderStatus = ? WHERE orderId = ?";
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

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
