public enum TransactionStatus {
    PENDING,     // Transaction is created but not yet completed
    COMPLETED,   // Transaction was completed successfully
    FAILED,      // Transaction failed due to some error
    REFUNDED,    // Transaction was refunded
    CANCELED     // Transaction was canceled by the user or system
}
