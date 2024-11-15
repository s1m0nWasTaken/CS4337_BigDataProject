package CS4337.Project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Entity
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int sourceUserId;
  private int shopItemId;
  private int quantity;
  private double amount;
  private String transactionStatus;
  private Date timeStamp;

  // Getters and Setters
}
