package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {

  @Mock private RatingRepository ratingRepository;

  @InjectMocks private RatingController ratingController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testAddRating_Success() {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great shop!",
            "rating", 5);

    when(ratingRepository.checkRating(anyInt(), anyInt())).thenReturn(List.of());
    when(ratingRepository.addRating(anyInt(), anyInt(), anyString(), anyInt())).thenReturn(1);

    ResponseEntity<Map<String, String>> response = ratingController.addRating(payload);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Rating added successfully", response.getBody().get("result"));
  }

  @Test
  public void testAddRating_RatingAlreadyExists() {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great shop!",
            "rating", 5);

    when(ratingRepository.checkRating(anyInt(), anyInt())).thenReturn(List.of(new Rating()));

    ResponseEntity<Map<String, String>> response = ratingController.addRating(payload);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Rating already exists", response.getBody().get("error"));
  }

  @Test
  public void testAddRating_MissingParameter() {
    Map<String, Object> payload =
        Map.of(
            "shopid", 1,
            "userid", 1,
            "message", "Great shop!");

    ResponseEntity<Map<String, String>> response = ratingController.addRating(payload);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("All parameters are required", response.getBody().get("error"));
  }

  @Test
  public void testUpdateRating_Success() {
    Map<String, Object> payload = Map.of("message", "Updated message", "rating", 4);

    when(ratingRepository.updateRating(anyInt(), anyString(), anyInt())).thenReturn(1);

    ResponseEntity<Map<String, String>> response = ratingController.updateRating(1, payload);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Rating updated successfully", response.getBody().get("result"));
  }

  @Test
  public void testUpdateRating_MissingParameter() {
    Map<String, Object> payload = Map.of("message", "Updated message");

    ResponseEntity<Map<String, String>> response = ratingController.updateRating(1, payload);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("All parameters are required", response.getBody().get("error"));
  }

  @Test
  public void testDeleteRating_Success() {
    when(ratingRepository.deleteRating(anyInt())).thenReturn(1);

    ResponseEntity<Map<String, String>> response = ratingController.deleteRating(1);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Rating deleted successfully", response.getBody().get("result"));
  }

  @Test
  public void testGetRatingsByShopId_Success() {
    Rating rating = new Rating();
    rating.setId(1);
    rating.setShopid(1);
    rating.setUserid(1);
    rating.setMessage("Good shop");
    rating.setRating(5);

    when(ratingRepository.getRatingByShopId(anyInt())).thenReturn(List.of(rating));

    ResponseEntity<List<Rating>> response = ratingController.getRatingsByShopId(1);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(1, response.getBody().size());
    assertEquals("Good shop", response.getBody().get(0).getMessage());
  }
}
