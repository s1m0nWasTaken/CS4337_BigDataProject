package CS4337.Project;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.ShopItem;
import CS4337.Project.Shared.Models.Transaction;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

  @Autowired
  @Qualifier("msRestTemplate") private RestTemplate restTemplate;

  @Autowired private TransactionRepository transactionRepository;

  private final String SHOP_SERVICE_URL = "http://SHOPSERVICE/";

  public ResponseEntity<?> createTransaction(TransactionRequest request) {
    ResponseEntity<Map<String, ShopItem>> shopItemResponse =
        getShopItemById(request.getShopItemId());
    if (shopItemResponse.getStatusCode() != HttpStatus.OK) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid shop item");
    }

    Map<String, ShopItem> shopItemHldr = shopItemResponse.getBody();
    ShopItem shopItem = shopItemHldr.get("success");

    if (shopItem.getStock() < request.getQuantity()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
    }

    Transaction transaction = new Transaction();
    transaction.setSourceUserId(request.getUserId());
    transaction.setAmount(request.getQuantity());
    transaction.setTransactionStatus("PENDING");
    transaction.setTimeStamp(new Date());
    transactionRepository.save(transaction);

    int newQuantity = shopItem.getStock() - request.getQuantity();
    updateShopItemStock(request.getShopItemId(), newQuantity);

    transaction.setTransactionStatus("SUCCESS");
    transactionRepository.save(transaction);

    return ResponseEntity.ok(transaction);
  }

  public Transaction getTransaction(int transactionId) {
    return transactionRepository.findById(transactionId).orElse(null);
  }

  public boolean isUserAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String role =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(null);

    return role.equalsIgnoreCase("ROLE_admin");
  }

  public int getUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return Integer.parseInt((String) auth.getPrincipal());
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
