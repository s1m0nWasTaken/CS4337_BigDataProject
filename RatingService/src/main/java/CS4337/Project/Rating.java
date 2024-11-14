package CS4337.Project;

import lombok.Data;

@Data
public class Rating {
  private int id;
  private int shopid;
  private int userid;
  private String message;
  private int rating;
}
