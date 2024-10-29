package CS4337.Project;

enum UserType {
  admin,
  shopowner,
  customer
}

public class User {
  private String id;
  private UserType userType;
  private String username;
  private String email;
  private String address;
  private boolean isHidden;

  public User() {}

  public User(UserType type, String username, String email, String address) {
    this.userType = type;
    this.username = username;
    this.email = email;
    this.address = address;
    this.isHidden = false;
  }

  public User(
      String id, String type, String username, String email, String address, boolean isHidden) {
    this.id = id;
    this.userType = UserType.valueOf(type);
    this.username = username;
    this.email = email;
    this.address = address;
    this.isHidden = isHidden;
  }

  public String getId() {
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

  public String getAddress() {
    return this.address;
  }

  public boolean getIsHidden() {
    return this.isHidden;
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

  public void setAddress(String address) {
    this.address = address;
  }

  public void setIsHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }
}
