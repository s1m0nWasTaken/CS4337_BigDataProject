package CS4337.Project;

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
    return paymentService.getTransactionStatus(transactionId);
  }
}

class TransactionRequest {
  private int userId;
  private int shopItemId;
  private int quantity;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getShopItemId() {
    return shopItemId;
  }

  public void setShopItemId(int shopItemId) {
    this.shopItemId = shopItemId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
