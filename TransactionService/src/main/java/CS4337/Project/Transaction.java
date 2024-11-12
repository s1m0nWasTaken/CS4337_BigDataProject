package CS4337.Project;

public class Transaction {
    private int id;
    private int sourceUserid;
    private double amount;
    private TransactionStatus transactionStatus;
    private String timeStamp;
    private boolean isRefunded;

    public Transaction() {
    }

    public Transaction(int id, int sourceUserid, double amount, TransactionStatus transactionStatus, String timeStamp, boolean isRefunded) {
        this.id = id;
        this.sourceUserid = sourceUserid;
        this.amount = amount;
        this.transactionStatus = transactionStatus;
        this.timeStamp = timeStamp;
        this.isRefunded = isRefunded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isRefunded() {
        return isRefunded;
    }

    public void setRefunded(boolean refunded) {
        isRefunded = refunded;
    }

    @Override
    public String toString() {
        return "CS4337.Project.Transaction{" +
                "id=" + id +
                ", sourceUserid=" + sourceUserid +
                ", amount=" + amount +
                ", transactionStatus=" + transactionStatus +
                ", timeStamp='" + timeStamp + '\'' +
                ", isRefunded=" + isRefunded +
                '}';
    }
}