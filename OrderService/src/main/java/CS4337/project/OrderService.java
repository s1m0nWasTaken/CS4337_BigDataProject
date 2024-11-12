package CS4337.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class OrderService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(OrderService.class, args);
    }

    //create an order - process is make a call to transaction, if that is successful, make the order.
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> addOrder(@RequestBody Order order) {
        try {
            TransactionRequest transaction = new TransactionRequest();
            // setting person ordering to the owner of the shop while auth is being figured out
            transaction.setSourceUserid(order.getShopItemid());
            transaction.setAmount(order.getPrice());
            transaction.setTransactionStatus(order.getOrderStatus().toString()); // Aligning order status with transaction status
            transaction.setTimeStamp(LocalDateTime.now()); // Set current timestamp
            transaction.setRefunded(false); // Default to not refunded

            // Call the transaction service
            String transactionUrl = "http://localhost:8080/transaction";
            ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(transactionUrl, transaction, Map.class);

            if (transactionResponse.getStatusCode() == HttpStatus.OK && transactionResponse.getBody() != null) {
                Map responseBody = transactionResponse.getBody();

                if (responseBody.containsKey("transactionId")) {
                    int transactionId = Integer.parseInt(responseBody.get("transactionId").toString());

                    // Set the transactionId in the Order object
                    order.setTransactionid(transactionId);

                    // Insert the order into the database
                    String sqlInsert = "INSERT INTO Orders (orderStatus, deliveryAddress, shopItemid, transactionid, price) VALUES (?, ?, ?, ?, ?)";
                    jdbcTemplate.update(
                            sqlInsert,
                            order.getOrderStatus().name(),
                            order.getDeliveryAddress(),
                            order.getShopItemid(),
                            order.getTransactionid(),
                            order.getPrice()
                    );

                    return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", 1, "message", "Order created successfully", "transactionId", transactionId));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Transaction failed, order not created"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to create transaction"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to connect to transaction service: " + e.getMessage()));
        }
    }


    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable int orderId, @RequestParam String status) {

        // TODO add checking that only admin or shopowner can do this
        try {
            String sqlUpdate = "UPDATE Orders SET orderStatus = ? WHERE orderId = ?";
            int rowsAffected = jdbcTemplate.update(sqlUpdate, status, orderId);

            if (rowsAffected > 0) {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", 1, "message", "Order status updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update order status: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(@RequestParam int page, @RequestParam int size) {
        // TODO need to add checking that it only returns all if user is admin and returns specified ones for normal user

        // TODO additional checking for shop owners needed too - logic is to join order -> shopitem -> shop and get all orders related to your shop
        try {
            String sqlQuery = "SELECT * FROM Orders LIMIT ? OFFSET ?";
            Object[] params = {size, page * size};

            List<Map<String, Object>> orders = jdbcTemplate.queryForList(sqlQuery, params);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", 1, "orders", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve orders: " + e.getMessage()));
        }
    }

}
