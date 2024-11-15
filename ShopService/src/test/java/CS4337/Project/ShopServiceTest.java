package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class ShopServiceTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private ShopService shopService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetAllShops_Success() {
    List<Map<String, Object>> mockShops = new ArrayList<>();
    mockShops.add(Map.of("id", 1, "shopName", "Test Shop"));

    when(jdbcTemplate.queryForList("SELECT * FROM Shop")).thenReturn(mockShops);

    ResponseEntity<Map<String, Object>> response = shopService.shop();

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(mockShops, response.getBody().get("success"));
  }

  @Test
  public void testAddShop_Success() {
    Shop shop =
        new Shop(
            1, 1, "ShopName", "Description", "ImageData", ShopType.CLOTHING, "email@example.com");

    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    Map<String, Object> response = shopService.addShop(shop);

    assertEquals(1, response.get("success"));
  }

  @Test
  public void testGetAllShopItems_Success() {
    List<Map<String, Object>> mockItems = new ArrayList<>();
    mockItems.add(Map.of("id", 1, "itemName", "Test Item"));

    when(jdbcTemplate.queryForList("SELECT * FROM ShopItem")).thenReturn(mockItems);

    ResponseEntity<Map<String, Object>> response = shopService.shopItem();

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(mockItems, response.getBody().get("success"));
  }

  @Test
  public void testAddShopItem_Success() {
    ShopItem shopItem = new ShopItem();
    shopItem.setShopid(1);
    shopItem.setPrice(20.0);
    shopItem.setItemName("ItemName");
    shopItem.setStock(10);
    shopItem.setPicture("PictureData");
    shopItem.setDescription("Description");

    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    Map<String, Object> response = shopService.addShopItem(shopItem);

    assertEquals(1, response.get("success"));
  }

  @Test
  public void testUpdateShopItem_Success() {
    ShopItem shopItem = new ShopItem();
    shopItem.setPrice(15.0);
    shopItem.setItemName("NewItem");
    shopItem.setStock(5);
    shopItem.setPicture("NewPicture");
    shopItem.setDescription("NewDescription");

    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    Map<String, Object> response = shopService.updateShopItem(1, shopItem);

    assertEquals(1, response.get("success"));
    assertEquals("Shop item updated successfully", response.get("message"));
  }

  @Test
  public void testUpdateShop_Success() {
    Shop shop =
        new Shop(
            1,
            1,
            "New Shop",
            "NewDescription",
            "NewImage",
            ShopType.ELECTRONICS,
            "new_email@example.com");

    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    Map<String, Object> response = shopService.updateShop(1, shop);

    assertEquals(1, response.get("success"));
    assertEquals("Shop updated successfully", response.get("message"));
  }

  @Test
  public void testDeleteShop_Success() {
    when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

    Map<String, Object> response = shopService.deleteShop(1);

    assertEquals(1, response.get("success"));
    assertEquals("Shop deleted successfully", response.get("message"));
  }

  @Test
  public void testDeleteShopItem_Success() {
    when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

    Map<String, Object> response = shopService.deleteShopItem(1);

    assertEquals(1, response.get("success"));
    assertEquals("Shop item deleted successfully", response.get("message"));
  }

  @Test
  public void testBanShopItem_Success() {
    Map<String, String> requestBody = Map.of("isHidden", "true");

    when(jdbcTemplate.update(anyString(), eq(true), eq(1))).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = shopService.banShopItem(1, requestBody);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(true, response.getBody().get("Shop item hidden"));
  }
}
