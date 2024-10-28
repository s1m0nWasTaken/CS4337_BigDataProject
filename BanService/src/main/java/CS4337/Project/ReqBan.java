package CS4337.Project;

import java.sql.Date;

public class ReqBan {
  private int banedId;
  private Date suspendedUntil;

  public Date getSuspendedUntil() {
    return suspendedUntil;
  }

  public void setSuspendedUntil(Date suspendedUntil) {
    this.suspendedUntil = suspendedUntil;
  }

  public int getBanedId() {
    return banedId;
  }

  public void setBanedId(int banedId) {
    this.banedId = banedId;
  }
}
