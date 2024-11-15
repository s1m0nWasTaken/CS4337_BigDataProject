package CS4337.project;

class TransactionRequest {
  private int userId;
  private int shopItemId;
  private int quantity;

  // Getters and Setters
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
