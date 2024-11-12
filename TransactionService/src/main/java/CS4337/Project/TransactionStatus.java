package CS4337.Project;

public enum TransactionStatus {
  PENDING, // CS4337.Project.Transaction is created but not yet completed
  COMPLETED, // CS4337.Project.Transaction was completed successfully
  FAILED, // CS4337.Project.Transaction failed due to some error
  REFUNDED, // CS4337.Project.Transaction was refunded
  CANCELED // CS4337.Project.Transaction was canceled by the user or system
}
