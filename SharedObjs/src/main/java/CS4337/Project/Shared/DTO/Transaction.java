package CS4337.Project.Shared.DTO;

import java.sql.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Transaction {
  private int id;
  private int sourceUserId;
  private double amount;
  private String transactionStatus;
  private Date timeStamp;
}
