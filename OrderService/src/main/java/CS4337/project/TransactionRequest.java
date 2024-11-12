package CS4337.project;

import java.time.LocalDateTime;

// THIS IS HERE COS I DIDN'T KNOW HOW ELSE TO IMPORT AN OBJECT FROM A DIFF MICROSERVICE HAHA HEHE : )
public class TransactionRequest {
    private int sourceUserid;
    private double amount;
    private String transactionStatus;
    private LocalDateTime timeStamp;
    private boolean refunded;

    // Getters and setters
    public int getSourceUserid() {
        return sourceUserid;
    }

    public void setSourceUserid(int sourceUserid) {
        this.sourceUserid = sourceUserid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }
}
