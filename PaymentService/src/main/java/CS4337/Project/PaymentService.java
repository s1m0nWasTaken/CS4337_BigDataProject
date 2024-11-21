package CS4337.Project;

import static CS4337.Project.RabbitMQConfig.DIR_EXCHANGE;
import static CS4337.Project.RabbitMQConfig.SHOP_ROUTING_KEY;

import java.util.Date;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {
  @Autowired private RestTemplate restTemplate;
  @Autowired private RabbitTemplate rabbitTemplate;

  @Autowired private TransactionRepository transactionRepository;

  private final String SHOP_SERVICE_URL = "http://SHOPSERVICE/";

  // dont need user checking here bc auth wouldalready have done that
  public ResponseEntity<?> createTransaction(TransactionRequest request) {
    // TODO: put back
    /*ResponseEntity<Map<String, ShopItem>> shopItemResponse =
        getShopItemById(request.getShopItemId());
    if (shopItemResponse.getStatusCode() != HttpStatus.OK) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid shop item");
    }

    Map<String, ShopItem> shopItemHldr = shopItemResponse.getBody();
    ShopItem shopItem = shopItemHldr.get("success");*/

    ShopItem shopItem = new ShopItem();
    shopItem.setItemName("book");
    shopItem.setDescription("desc");
    shopItem.setPrice(100);
    shopItem.setId(3);
    shopItem.setStock(10);
    shopItem.setPicture("pic");
    shopItem.setShopid(4);
    /*if (shopItem.getStock() < request.getQuantity()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
    }*/

    Transaction transaction = new Transaction();
    transaction.setSourceUserId(request.getUserId());
    transaction.setAmount(2.0 * request.getQuantity());
    transaction.setTransactionStatus("PENDING");
    transaction.setTimeStamp(new Date());
    transactionRepository.save(transaction);

    int newQuantity = 5 - request.getQuantity();
    updateShopItemStock(1, newQuantity);

    transaction.setTransactionStatus("SUCCESS");
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
    String json = "{" + "\"shopItem\":" + shopItemId + ", \"newQuantity\":" + newQuantity + "}";

    rabbitTemplate.convertAndSend(DIR_EXCHANGE, SHOP_ROUTING_KEY, json);
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
