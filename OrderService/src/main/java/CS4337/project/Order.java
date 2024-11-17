package CS4337.project;

public class Order {
  private int id;
  private OrderStatus orderStatus;
  private String deliveryAddress;
  private int shopItemid;
  private int transactionid;
  private double price;
  private int userId; // Added this field based on your unfinished line.

  public Order() {}

  public Order(
      int id,
      OrderStatus orderStatus,
      String deliveryAddress,
      int shopItemid,
      int transactionid,
      double price,
      int userId) {
    this.id = id;
    this.orderStatus = orderStatus;
    this.deliveryAddress = deliveryAddress;
    this.shopItemid = shopItemid;
    this.transactionid = transactionid;
    this.price = price;
    this.userId = userId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public OrderStatus getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
  }

  public String getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(String deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
  }

  public int getShopItemid() {
    return shopItemid;
  }

  public void setShopItemid(int shopItemid) {
    this.shopItemid = shopItemid;
  }

  public int getTransactionid() {
    return transactionid;
  }

  public void setTransactionid(int transactionid) {
    this.transactionid = transactionid;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
}
