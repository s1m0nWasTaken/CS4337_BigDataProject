package CS4337.Project;

public class GoogleUserInfo {
  private String sub; // UUID
  private String name;
  private String givenName;
  private String familyName;
  private String picture;
  private String email;
  private boolean emailVerified;
  private String locale;

  public String getSub() {
    return sub;
  }

  public String getName() {
    return name;
  }

  public String getGivenName() {
    return givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public String getPicture() {
    return picture;
  }

  public String getEmail() {
    return email;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public String getLocale() {
    return locale;
  }
}
