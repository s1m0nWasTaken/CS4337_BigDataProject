package CS4337.Project;

import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

  @Autowired private RestTemplate restTemplate;

  @Autowired private TransactionRepository transactionRepository;

  private final String SHOP_SERVICE_URL = "http://SHOPSERVICE/";
  private final String USER_SERVICE_URL = "http://USERSERVICE/";

  // dont need user checking here bc auth wouldalready have done that
  public ResponseEntity<?> createTransaction(TransactionRequest request) {

    ResponseEntity<Map<String, ShopItem>> shopItemResponse =
        getShopItemById(request.getShopItemId());
    if (shopItemResponse.getStatusCode() != HttpStatus.OK) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid shop item");
    }

    System.out.println("0");

    System.out.println(shopItemResponse.getBody());
    Map<String, ShopItem> shopItemHldr = shopItemResponse.getBody();
    ShopItem shopItem = shopItemHldr.get("success");

    System.out.println("1");

    if (shopItem.getStock() < request.getQuantity()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
    }

    System.out.println("2");
    Transaction transaction = new Transaction();
    transaction.setSourceUserId(request.getUserId());
    transaction.setShopItemId(request.getShopItemId());
    transaction.setAmount(shopItem.getPrice() * request.getQuantity());
    transaction.setTransactionStatus("PENDING");
    transaction.setTimeStamp(new Date());
    System.out.println("3");

    transactionRepository.save(transaction);

    System.out.println("4");
    int newQuantity = shopItem.getStock() - request.getQuantity();
    updateShopItemStock(shopItem.getId(), newQuantity);

    System.out.println("5");
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

  private ResponseEntity<Map<String, ShopItem>> getShopItemById(int shopItemId) {
    try {

      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
      ResponseEntity<Map<String, ShopItem>> resJson =
          restTemplate.exchange(
              SHOP_SERVICE_URL + "shopItem/{id}",
              HttpMethod.GET,
              requestEntity,
              new ParameterizedTypeReference<Map<String, ShopItem>>() {},
              shopItemId);
      return resJson;
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private void updateShopItemStock(int shopItemId, int newQuantity) {
    Map json = Map.of("stock", newQuantity);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(json);
    ResponseEntity<Map> response =
        restTemplate.exchange(
            SHOP_SERVICE_URL + "shopItem/" + shopItemId, HttpMethod.PUT, requestEntity, Map.class);
  }
}

// TODO DRY this
class ShopItem {
  private int id;
  private int shopid;
  private double price = -1;
  private String itemName;
  private int stock = -1;
  private String description;
  private String picture;
  private boolean isHidden;
  private boolean canUpdate;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getShopid() {
    return shopid;
  }

  public void setShopid(int shopid) {
    this.shopid = shopid;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean hidden) {
    isHidden = hidden;
  }

  public boolean isCanUpdate() {
    return canUpdate;
  }

  public void setCanUpdate(boolean canUpdate) {
    this.canUpdate = canUpdate;
  }

  // Method for easily updating stock
  public void updateStock(int amount) {
    setStock(getStock() - amount);
  }
}
