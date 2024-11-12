package CS4337.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class TransactionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(TransactionService.class, args);
    }

    // Retrieve transactions with optional filters
    @GetMapping("/transaction")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) Integer sourceUserid,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        int maxItemsShown = 50;
        if (pageSize > maxItemsShown) {
            pageSize = maxItemsShown;
        }

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Transaction WHERE 1=1");

        if (sourceUserid != null) {
            sql.append(" AND sourceUserid = ?");
            params.add(sourceUserid);
        }

        if (minAmount != null) {
            sql.append(" AND amount >= ?");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append(" AND amount <= ?");
            params.add(maxAmount);
        }

        sql.append(" LIMIT ?");
        params.add(pageSize);

        sql.append(" OFFSET ?");
        params.add(page * pageSize);

        try {
            List<Map<String, Object>> transactions = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", transactions));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transaction")
    public Map<String, Object> addTransaction(@RequestBody Transaction transaction) {
        // Step 1: Call the fake payment service
        try {
            String fakePaymentServiceUrl = "http://localhost:8080/payment/process";
            ResponseEntity<String> paymentResponse = restTemplate.postForEntity(fakePaymentServiceUrl, null, String.class);

            if (paymentResponse.getStatusCode() == HttpStatus.OK) {                // Step 2: Proceed with saving the transaction if payment is successful
                String sqlInsert =
                        "INSERT INTO Transaction (sourceUserid, amount, transactionStatus, timeStamp, isRefunded) "
                                + "VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(
                        sqlInsert,
                        transaction.getSourceUserid(),
                        transaction.getAmount(),
                        transaction.getTransactionStatus().name(),
                        transaction.getTimeStamp(),
                        transaction.isRefunded());
                return Map.of("success", 1, "message", "Transaction processed and saved successfully");
            } else {
                // Handle payment failure
                return Map.of("error", "Payment failed, transaction not processed");
            }
        } catch (Exception e) {
            return Map.of("error", "Failed to connect to payment service: " + e.getMessage());
        }
    }

    @PutMapping("/transaction/status/{id}")
    public Map<String, Object> updateTransactionStatus(@PathVariable int id, @RequestBody Transaction transaction) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE Transaction SET ");

        if (transaction.getTransactionStatus() != null) {
            sql.append("transactionStatus = ?, ");
            params.add(transaction.getTransactionStatus().name());
        }

        sql.append(" WHERE id = ?");
        params.add(id);

        try {
            int rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());
            if (rowsAffected > 0) {
                return Map.of("success", 1, "message", "Transaction updated successfully");
            } else {
                return Map.of("success", 0, "message", "Transaction not found");
            }
        } catch (DataAccessException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PutMapping("/transaction/refund/{id}")
    public Map<String, Object> updateTransactionRefundState(@PathVariable int id, @RequestBody Transaction transaction) {
        // add validation so only site admin can use this endpoint
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE Transaction SET ");

        sql.append(" SET isRefunded = ?");
        params.add(transaction.isRefunded());

        sql.append(" WHERE id = ?");
        params.add(id);

        try {
            int rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());
            if (rowsAffected > 0) {
                return Map.of("success", 1, "message", "Transaction updated successfully");
            } else {
                return Map.of("success", 0, "message", "Transaction not found");
            }
        } catch (DataAccessException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @DeleteMapping("/transaction/{id}")
    public Map<String, Object> deleteTransaction(@PathVariable int id) {
        String deleteQuery = "DELETE FROM Transaction WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(deleteQuery, id);
            if (rowsAffected > 0) {
                return Map.of("success", 1, "message", "Transaction deleted successfully");
            } else {
                return Map.of("success", 0, "message", "Transaction not found");
            }
        } catch (DataAccessException e) {
            return Map.of("error", e.getMessage());
        }
    }
}
