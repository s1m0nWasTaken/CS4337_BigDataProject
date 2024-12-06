package CS4337.Project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import CS4337.Project.Shared.DTO.TransactionRequest;
import CS4337.Project.Shared.Models.ShopItem;
import CS4337.Project.Shared.Models.Transaction;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

  @Mock private TransactionRepository transactionRepository;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private PaymentService paymentService;

  private TransactionRequest testRequest;
  private Transaction testTransaction;

  @BeforeEach
  void setUp() {
    testRequest = new TransactionRequest();
    testRequest.setUserId(1);
    testRequest.setShopItemId(1);
    testRequest.setQuantity(2);

    testTransaction = new Transaction();
    testTransaction.setId(1);
    testTransaction.setSourceUserId(1);
    testTransaction.setAmount(4.0);
    testTransaction.setTransactionStatus("SUCCESS");
    testTransaction.setTimeStamp(new Date());
  }

  @Test
  void getTransaction_WhenExists() {
    when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
    Transaction result = paymentService.getTransaction(1);
    assertNotNull(result);
    assertEquals(1, result.getId());
    assertEquals("SUCCESS", result.getTransactionStatus());
  }

  @Test
  void getTransaction_WhenNotExists() {
    when(transactionRepository.findById(999)).thenReturn(Optional.empty());
    Transaction result = paymentService.getTransaction(999);
    assertNull(result);
  }

  @Test
  void createTransaction_Failure_InvalidShopItem() {
    testRequest.setQuantity(0);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            anyInt()))
        .thenReturn(ResponseEntity.notFound().build());
    ResponseEntity<?> response = paymentService.createTransaction(testRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid shop item", response.getBody());
  }

  @Test
  void createTransaction_Success() {
    Transaction savedTransaction = new Transaction();
    savedTransaction.setTransactionStatus("SUCCESS");

    ShopItem shopItem = new ShopItem();
    shopItem.setStock(5);
    ResponseEntity<Map<String, ShopItem>> shopResponse =
        ResponseEntity.ok(Map.of("success", shopItem));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            anyInt()))
        .thenReturn(shopResponse);

    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok().build());

    ResponseEntity<?> response = paymentService.createTransaction(testRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Transaction returnedTransaction = (Transaction) response.getBody();
    assertNotNull(returnedTransaction);
    assertEquals(
        savedTransaction.getTransactionStatus(), returnedTransaction.getTransactionStatus());
    verify(transactionRepository, times(2)).save(any(Transaction.class));
  }

  @Test
  void createTransaction_InsufficientStock() {
    testRequest.setQuantity(10);
    ShopItem shopItem = new ShopItem();
    shopItem.setStock(5);
    ResponseEntity<Map<String, ShopItem>> shopResponse =
        ResponseEntity.ok(Map.of("success", shopItem));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            anyInt()))
        .thenReturn(shopResponse);

    ResponseEntity<?> response = paymentService.createTransaction(testRequest);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Insufficient stock", response.getBody());
  }

  @Test
  void createTransaction_InvalidQuantity() {
    testRequest.setQuantity(0);
    ShopItem shopItem = new ShopItem();
    shopItem.setStock(5);

    ResponseEntity<Map<String, String>> shopResponse =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid quantity"));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            eq(testRequest.getShopItemId())))
        .thenReturn(shopResponse);

    ResponseEntity<?> response = paymentService.createTransaction(testRequest);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid shop item", response.getBody());
  }

  @Test
  void createTransaction_InvalidUser() {
    testRequest.setUserId(-1);
    ShopItem shopItem = new ShopItem();
    shopItem.setStock(5);
    ResponseEntity<Map<String, ShopItem>> shopResponse =
        ResponseEntity.ok(Map.of("success", shopItem));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            anyInt()))
        .thenReturn(shopResponse);

    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(
            invocation -> {
              Transaction t = invocation.getArgument(0);
              return t;
            });

    ResponseEntity<?> response = paymentService.createTransaction(testRequest);

    verify(transactionRepository, times(2))
        .save(argThat(transaction -> transaction.getSourceUserId() == -1));
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void createTransaction_NullRequest() {
    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> {
              paymentService.createTransaction(null);
            });
    assertEquals(
        "Cannot invoke \"CS4337.Project.Shared.DTO.TransactionRequest.getShopItemId()\" because \"request\" is null",
        exception.getMessage());
  }

  @Test
  void createTransaction_DuplicateTransaction() {
    ShopItem shopItem = new ShopItem();
    shopItem.setStock(5);
    ResponseEntity<Map<String, ShopItem>> shopResponse =
        ResponseEntity.ok(Map.of("success", shopItem));

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            any(ParameterizedTypeReference.class),
            anyInt()))
        .thenReturn(shopResponse);

    when(transactionRepository.save(any(Transaction.class)))
        .thenThrow(new IllegalArgumentException("Duplicate transaction"));

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              paymentService.createTransaction(testRequest);
            });

    assertEquals("Duplicate transaction", exception.getMessage());
  }
}
