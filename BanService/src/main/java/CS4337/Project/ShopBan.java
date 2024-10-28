package CS4337.Project;

import java.sql.Date;

public class ShopBan {
  private int id;
  private int shopItemId;
  private Date suspendedUntil;
  private Boolean isActive;

  public ShopBan(int id, int shopItemId, Date suspendedUntil, Boolean isActive) {
    this.id = id;
    this.shopItemId = shopItemId;
    this.suspendedUntil = suspendedUntil;
    this.isActive = isActive;
  }

  public ShopBan() {}

  public int getId() {
    return id;
  }

  public int getShopItemId() {
    return shopItemId;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getSuspendedUntil() {
    return suspendedUntil;
  }

  public void setSuspendedUntil(Date suspendedUntil) {
    this.suspendedUntil = suspendedUntil;
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  public boolean getIsActive() {
    return this.isActive;
  }
}
