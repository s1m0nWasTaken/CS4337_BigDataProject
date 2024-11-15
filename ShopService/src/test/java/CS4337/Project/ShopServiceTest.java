package CS4337.Project;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ShopService.class)
public class ShopServiceTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ShopService shopService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testGetShops() throws Exception {
    Map<String, Object> shop =
        Map.of("id", 1, "shopName", "Test Shop", "description", "A test shop", "shopOwnerid", 123);

    when(shopService.shop(any(), any(), any(), eq(0), eq(50)))
        .thenReturn(ResponseEntity.ok(Map.of("success", List.of(shop))));

    mockMvc
        .perform(get("/shop?page=0&pageSize=50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success[0].id").value(1))
        .andExpect(jsonPath("$.success[0].shopName").value("Test Shop"));
  }

  @Test
  public void testAddShop() throws Exception {
    Shop shop = new Shop();
    shop.setShopName("Test Shop");
    shop.setDescription("A test shop");
    shop.setShopOwnerid(123);

    when(shopService.addShop(any())).thenReturn(Map.of("success", 1));

    mockMvc
        .perform(
            post("/shop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shop)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1));
  }

  @Test
  public void testUpdateShop() throws Exception {
    Shop shop = new Shop();
    shop.setShopName("Updated Shop");
    shop.setDescription("Updated description");

    when(shopService.updateShop(eq(1), any()))
        .thenReturn(Map.of("success", 1, "message", "Shop updated successfully"));

    mockMvc
        .perform(
            put("/shop/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shop)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1))
        .andExpect(jsonPath("$.message").value("Shop updated successfully"));
  }

  @Test
  public void testDeleteShop() throws Exception {
    when(shopService.deleteShop(eq(1)))
        .thenReturn(Map.of("success", 1, "message", "Shop deleted successfully"));

    mockMvc
        .perform(delete("/shop/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1))
        .andExpect(jsonPath("$.message").value("Shop deleted successfully"));
  }

  @Test
  public void testGetShopItems() throws Exception {
    Map<String, Object> shopItem = Map.of("id", 1, "itemName", "Test Item", "price", 99.99);

    when(shopService.shopItem(any(), any(), any(), any(), eq(0), eq(50)))
        .thenReturn(ResponseEntity.ok(Map.of("success", List.of(shopItem))));

    mockMvc
        .perform(get("/shopItem?page=0&pageSize=50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success[0].id").value(1))
        .andExpect(jsonPath("$.success[0].itemName").value("Test Item"))
        .andExpect(jsonPath("$.success[0].price").value(99.99));
  }

  @Test
  public void testAddShopItem() throws Exception {
    ShopItem shopItem = new ShopItem();
    shopItem.setItemName("Test Item");
    shopItem.setPrice(99.99);
    shopItem.setShopid(1);

    when(shopService.addShopItem(any())).thenReturn(Map.of("success", 1));

    mockMvc
        .perform(
            post("/shopItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shopItem)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1));
  }

  @Test
  public void testUpdateShopItem() throws Exception {
    ShopItem shopItem = new ShopItem();
    shopItem.setItemName("Updated Item");
    shopItem.setPrice(89.99);

    when(shopService.updateShopItem(eq(1), any()))
        .thenReturn(Map.of("success", 1, "message", "Shop item updated successfully"));

    mockMvc
        .perform(
            put("/shopItem/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shopItem)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1))
        .andExpect(jsonPath("$.message").value("Shop item updated successfully"));
  }

  @Test
  public void testDeleteShopItem() throws Exception {
    when(shopService.deleteShopItem(eq(1)))
        .thenReturn(Map.of("success", 1, "message", "Shop item deleted successfully"));

    mockMvc
        .perform(delete("/shopItem/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(1))
        .andExpect(jsonPath("$.message").value("Shop item deleted successfully"));
  }

  @Test
  public void testBanShopItem() throws Exception {
    when(shopService.banShopItem(eq(1), any()))
        .thenReturn(ResponseEntity.ok(Map.of("Shop item hidden", true)));

    mockMvc
        .perform(
            put("/shopItem/ban/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("isHidden", "true"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$['Shop item hidden']").value(true));
  }
}
