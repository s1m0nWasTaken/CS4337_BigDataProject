package CS4337.Project.RatingService;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
public class RatingController {
  private final RatingRepository ratingRepository;

  @Autowired
  public RatingController(RatingRepository ratingRepository) {
    this.ratingRepository = ratingRepository;
  }

  @PostMapping("/add")
  public ResponseEntity<Map<String, String>> addRating(@RequestBody Map<String, Object> payload) {
    try {
      Integer shopid = (Integer) payload.get("shopid");
      Integer userid = (Integer) payload.get("userid");
      String message = (String) payload.get("message");
      Integer rating = (Integer) payload.get("rating");

      if (shopid == null || userid == null || message == null || rating == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "All parameters are required"));
      }
      int amountOfRating = ratingRepository.checkRating(shopid, userid).size();
      if (amountOfRating > 0) {
        return ResponseEntity.badRequest().body(Map.of("error", "Rating already exists"));
      }

      int result = ratingRepository.addRating(shopid, userid, message, rating);
      return ResponseEntity.ok(
          Map.of(
              "result",
              result > 0 ? "Rating added successfully" : "Rating not added successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/update/{id}")
  public ResponseEntity<Map<String, String>> updateRating(
      @PathVariable int id, @RequestBody Map<String, Object> payload) {
    try {
      String message = (String) payload.get("message");
      Integer rating = (Integer) payload.get("rating");

      if (message == null || rating == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "All parameters are required"));
      }

      int result = ratingRepository.updateRating(id, message, rating);
      return ResponseEntity.ok(
          Map.of(
              "result",
              result > 0 ? "Rating updated successfully" : "Rating not updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Map<String, String>> deleteRating(@PathVariable int id) {
    int result = ratingRepository.deleteRating(id);
    return ResponseEntity.ok(
        Map.of(
            "result",
            result > 0 ? "Rating deleted successfully" : "Rating not deleted successfully"));
  }

  @GetMapping("/shop/{shopid}")
  public ResponseEntity<List<Rating>> getRatingsByShopId(@PathVariable int shopid) {
    List<Rating> ratings = ratingRepository.getRatingByShopId(shopid);
    return ResponseEntity.ok(ratings);
  }
}
