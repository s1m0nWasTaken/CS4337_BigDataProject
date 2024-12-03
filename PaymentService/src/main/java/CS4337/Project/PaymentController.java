package CS4337.Project;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @PostMapping("/create")
  public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request) {
    if (!(paymentService.isUserAdmin() || paymentService.getUserId() == request.getUserId())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("You are not authorized to create this transaction");
    }

    if (paymentService == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("PaymentService not available");
    }
    return paymentService.createTransaction(request);
  }

  @GetMapping("/status/{transactionId}")
  public ResponseEntity<?> getTransactionStatus(@PathVariable int transactionId) {
    if (paymentService == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("PaymentService not available");
    }
    Transaction transaction = paymentService.getTransaction(transactionId);

    if (!(paymentService.isUserAdmin()
        || paymentService.getUserId() == transaction.getSourceUserId())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("You are not authorized to view this transaction");
    }

    if (transaction == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
    }

    return ResponseEntity.ok(transaction);
  }
}
