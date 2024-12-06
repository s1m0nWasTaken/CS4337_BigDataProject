package CS4337.Project;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import CS4337.Project.Shared.Security.AuthUtils;
import CS4337.Project.Shared.Security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@WebMvcTest(RatingController.class)
@Import(TestSecurityConfig.class)
public class RatingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RatingRepository ratingRepository;

  @MockBean private RatingService ratingService;

  @MockBean private JwtUtils jwtUtils;

  @MockBean
  @Qualifier("authRestTemplate") private RestTemplate restTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockedStatic<AuthUtils> authUtilsMock;

  @BeforeEach
  void setUp() {
    lenient().doReturn(true).when(ratingService).isUserRatingOwner(anyInt());

    authUtilsMock = mockStatic(AuthUtils.class);
    when(AuthUtils.getUserId()).thenReturn(1);
  }

  @AfterEach
  public void tearDown() {
    if (authUtilsMock != null) {
      authUtilsMock.close();
    }
  }

  @Test
  public void testAddRating() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great service!",
            "rating", 5);

    when(ratingRepository.addRating(1, 1, "Great service!", 5)).thenReturn(1);
    when(ratingRepository.checkRating(1, 1)).thenReturn(List.of());

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating added successfully"));
  }

  @Test
  public void testUpdateRating() throws Exception {
    Map<String, Object> payload = Map.of("message", "Updated review", "rating", 4);

    when(ratingRepository.updateRating(1, "Updated review", 4)).thenReturn(1);

    mockMvc
        .perform(
            put("/ratings/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating updated successfully"));
  }

  @Test
  public void testDeleteRating() throws Exception {
    when(ratingRepository.deleteRating(1)).thenReturn(1);

    mockMvc
        .perform(delete("/ratings/delete/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating deleted successfully"));
  }

  @Test
  public void testGetRatingsByShopId() throws Exception {
    Rating rating = new Rating();
    rating.setId(1);
    rating.setShopid(1);
    rating.setUserid(1);
    rating.setMessage("Great service!");
    rating.setRating(5);

    when(ratingRepository.getRatingByShopId(1, 0, 50)).thenReturn(List.of(rating));

    mockMvc
        .perform(get("/ratings/shop/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].shopid").value(1))
        .andExpect(jsonPath("$[0].userid").value(1))
        .andExpect(jsonPath("$[0].message").value("Great service!"))
        .andExpect(jsonPath("$[0].rating").value(5));
  }

  @Test
  public void testAddRatingWithInvalidDataTypes() throws Exception {
    Map<String, Object> payload =
        Map.of("shopid", "invalid", "userid", 1, "message", "Great service!", "rating", 5);

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  public void testAddRatingWithNullValues() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("shopid", null);
    payload.put("userid", 1);
    payload.put("message", "Great service!");
    payload.put("rating", 5);

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("All parameters are required"));
  }

  @Test
  public void testAddRatingWithDatabaseFailure() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great service!",
            "rating", 5);

    when(ratingRepository.addRating(1, 1, "Great service!", 5))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Database error"));
  }

  @Test
  public void testAddRatingWithMaxMessageLength() throws Exception {
    String maxMessage = "a".repeat(255);
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", maxMessage,
            "rating", 5);

    when(ratingRepository.addRating(1, 1, maxMessage, 5)).thenReturn(1);

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating added successfully"));
  }

  @Test
  public void testAddRatingWithExistingRating() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great service!",
            "rating", 5);

    when(ratingRepository.checkRating(1, 1)).thenReturn(List.of(new Rating()));

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Rating already exists"));
  }

  @Test
  public void testAddRatingWithEmptyMessage() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "",
            "rating", 5);

    when(ratingRepository.addRating(1, 1, "", 5)).thenReturn(1);
    when(ratingRepository.checkRating(1, 1)).thenReturn(List.of());

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating added successfully"));
  }

  @Test
  public void testAddRatingWithNegativeRating() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Test",
            "rating", -1);

    when(ratingRepository.addRating(1, 1, "Test", -1)).thenReturn(1);
    when(ratingRepository.checkRating(1, 1)).thenReturn(List.of());

    mockMvc
        .perform(
            post("/ratings/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating added successfully"));
  }

  @Test
  public void testUpdateRatingUnauthorized() throws Exception {
    Map<String, Object> payload = Map.of("message", "Updated review", "rating", 4);

    when(ratingService.isUserRatingOwner(anyInt())).thenReturn(false);

    mockMvc
        .perform(
            put("/ratings/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("You do not have permission to edit this rating"));
  }

  @Test
  public void testUpdateRatingWithMissingParameters() throws Exception {
    Map<String, Object> payload = Map.of("message", "Updated review");

    mockMvc
        .perform(
            put("/ratings/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("All parameters are required"));
  }

  @Test
  public void testUpdateRatingWithEmptyPayload() throws Exception {
    mockMvc
        .perform(put("/ratings/update/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testDeleteRatingWithNonExistentId() throws Exception {
    when(ratingRepository.deleteRating(9999)).thenReturn(0);

    mockMvc
        .perform(delete("/ratings/delete/9999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Rating not deleted successfully"));
  }

  @Test
  public void testDeleteRatingUnauthorized() throws Exception {
    when(ratingService.isUserRatingOwner(anyInt())).thenReturn(false);

    mockMvc
        .perform(delete("/ratings/delete/1"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("You do not have permission to delete this rating"));
  }

  @Test
  public void testGetRatingsByShopIdEmptyList() throws Exception {
    when(ratingRepository.getRatingByShopId(1, 0, 50)).thenReturn(List.of());

    mockMvc
        .perform(get("/ratings/shop/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }
}
