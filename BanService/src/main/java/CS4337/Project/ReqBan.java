package CS4337.Project;

import java.sql.Date;

public class ReqBan {
  private int bannedId;
  private Date suspendedUntil;

  public Date getSuspendedUntil() { return suspendedUntil; }

  public void setSuspendedUntil(Date suspendedUntil) {
    this.suspendedUntil = suspendedUntil;
  }

  public int getBannedId() { return bannedId; }

  public void setBannedId(int bannedId) { this.bannedId = bannedId; }
}
