package CS4337.Project;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

  @Autowired private RestTemplate restTemplate;

  @Autowired private TransactionRepository transactionRepository;

  private final String SHOP_SERVICE_URL = "http://SHOPSERVICE/";
  private final String USER_SERVICE_URL = "http://USERSERVICE/";

  public ResponseEntity<?> createTransaction(TransactionRequest request) {
    ResponseEntity<User> userResponse = getUserById(request.getUserId());
    if (userResponse.getStatusCode() != HttpStatus.OK) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user");
    }

    ResponseEntity<ShopItem> shopItemResponse = getShopItemById(request.getShopItemId());
    if (shopItemResponse.getStatusCode() != HttpStatus.OK) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid shop item");
    }

    ShopItem shopItem = shopItemResponse.getBody();
    if (shopItem.getStock() < request.getQuantity()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
    }

    Transaction transaction = new Transaction();
    transaction.setSourceUserId(request.getUserId());
    transaction.setShopItemId(request.getShopItemId());
    transaction.setQuantity(request.getQuantity());
    transaction.setAmount(shopItem.getPrice() * request.getQuantity());
    transaction.setTransactionStatus("PENDING");
    transaction.setTimeStamp(new Date());

    transactionRepository.save(transaction);

    shopItem.setStock(shopItem.getStock() - request.getQuantity());
    updateShopItemStock(shopItem);

    transaction.setTransactionStatus("COMPLETED");
    transactionRepository.save(transaction);

    return ResponseEntity.ok(transaction);
  }

  public ResponseEntity<?> getTransactionStatus(int transactionId) {
    Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
    if (transaction == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
    }

    return ResponseEntity.ok(transaction);
  }

  private ResponseEntity<User> getUserById(int userId) {
    try {
      return restTemplate.getForEntity(USER_SERVICE_URL + "/user/" + userId, User.class);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private ResponseEntity<ShopItem> getShopItemById(int shopItemId) {
    try {
      return restTemplate.getForEntity(SHOP_SERVICE_URL + "/item/" + shopItemId, ShopItem.class);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private void updateShopItemStock(ShopItem shopItem) {
    try {
      restTemplate.put(SHOP_SERVICE_URL + "/item/updateStock", shopItem);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update shop item stock");
    }
  }
}

class User {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

class ShopItem {
  private int id;
  private String name;
  private double price;
  private int stock;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }
}
