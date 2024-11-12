package CS4337.Project;

import java.sql.Date;

public class UserBan {
  private int id;
  private int userId;
  private Date suspendedUntil;
  private Boolean isActive;

  public UserBan(int id, int userId, Date suspendedUntil, Boolean isActive) {
    this.id = id;
    this.userId = userId;
    this.suspendedUntil = suspendedUntil;
    this.isActive = isActive;
  }

  public UserBan() {}

  public int getId() {
    return id;
  }

  public Date getSuspendedUntil() {
    return suspendedUntil;
  }

  public int getUserId() {
    return userId;
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
