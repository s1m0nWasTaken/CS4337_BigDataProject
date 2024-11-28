package CS4337.Project.Shared.Models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShopItem {
  private int id;
  private int shopid;
  private double price = -1;
  private String itemName;
  private int stock = -1;
  private String description;
  private String picture;
  private boolean isHidden;
  private boolean canUpdate;

  // Method for easily updating stock
  public void updateStock(int amount) {
    setStock(getStock() - amount);
  }
}
