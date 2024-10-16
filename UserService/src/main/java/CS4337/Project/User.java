package CS4337.Project;

import java.sql.Date;

enum UserType {
  admin,
  shopowner,
  customer
}

public class User {
  private int id;
  private UserType userType;
  private String username;
  private String email;
  private String address;
  private Date suspendedUntil;

  public User() {}

  public User(UserType type, String username, String email, String address) {
    this.userType = type;
    this.username = username;
    this.email = email;
    this.address = address;
    this.suspendedUntil = Date.valueOf("1000-01-01");
  }

  public User(
      int id, UserType type, String username, String email, String address, Date suspendedUntil) {
    this.id = id;
    this.userType = type;
    this.username = username;
    this.email = email;
    this.address = address;
    this.suspendedUntil = suspendedUntil;
  }

  public int getId() {
    return this.id;
  }

  public UserType getUserType() {
    return this.userType;
  }

  public String getUsername() {
    return this.username;
  }

  public String getEmail() {
    return this.email;
  }

  public String getAdress() {
    return this.address;
  }

  public Date getSuspendedUntil() {
    return this.suspendedUntil;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setAdress(String address) {
    this.address = address;
  }

  public void setSuspendedUntil(Date suspendedUntil) {
    this.suspendedUntil = suspendedUntil;
  }
}
