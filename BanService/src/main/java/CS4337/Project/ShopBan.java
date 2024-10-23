package CS4337.Project;

import java.sql.Date;

public class ShopBan {
  private int id;
  private int shopId;
  private Date suspendedUntil;

  public ShopBan(int id, int shopId, Date suspendedUntil) {
    this.id = id;
    this.shopId = shopId;
    this.suspendedUntil = suspendedUntil;
  }

  public ShopBan() {}

  public int getId() {
    return id;
  }

  public int getShopId() {
    return shopId;
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
}
