package CS4337.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
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
            // Step 1: Create a Transaction object using details from the Order
            Transaction transaction = new Transaction();
            transaction.setSourceUserid(order.getShopItemid()); // Assuming shop item owner is source user
            transaction.setAmount(order.getPrice());
            transaction.setTransactionStatus(order.getOrderStatus()); // Aligning order status with transaction status
            transaction.setTimeStamp(LocalDateTime.now()); // Set current timestamp
            transaction.setRefunded(false); // Default to not refunded

            // Step 2: Call the transaction service
            String transactionUrl = "http://localhost:8080/transaction";
            ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(transactionUrl, transaction, Map.class);

            if (transactionResponse.getStatusCode() == HttpStatus.OK && transactionResponse.getBody() != null) {
                Map<String, Object> responseBody = transactionResponse.getBody();

                if (responseBody.containsKey("transactionId")) {
                    int transactionId = Integer.parseInt(responseBody.get("transactionId").toString());

                    // Step 3: Set the transactionId in the Order object
                    order.setTransactionid(transactionId);

                    // Step 4: Insert the order into the database
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


    //update order status

    //admin retrieve pagified result of all orders, user retrieve pagified result of their orders


}
