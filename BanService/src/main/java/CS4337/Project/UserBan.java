package CS4337.Project;

import java.sql.Date;

public class UserBan {
  private int id;
  private int userId;
  private Date suspendedUntil;

  public UserBan(int id, int userId, Date suspendedUntil) {
    this.id = id;
    this.userId = userId;
    this.suspendedUntil = suspendedUntil;
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
}
