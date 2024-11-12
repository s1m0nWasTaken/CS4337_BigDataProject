package CS4337.Project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentService {

    @PostMapping("/process")
    public ResponseEntity<String> processPayment() {
        // Simulate a payment process that always returns success
        return new ResponseEntity<>("Payment processed successfully", HttpStatus.OK);
    }
}
