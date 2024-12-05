package CS4337.Project;

import CS4337.Project.Shared.Security.AuthUtils;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
public class RatingController {
  private final RatingRepository ratingRepository;
  private final RatingService ratingService;

  @Autowired
  public RatingController(RatingRepository ratingRepository, RatingService ratingService) {
    this.ratingRepository = ratingRepository;
    this.ratingService = ratingService;
  }

  @PostMapping("/add")
  public ResponseEntity<Map<String, String>> addRating(@RequestBody Map<String, Object> payload) {
    try {
      Integer shopid = (Integer) payload.get("shopid");
      String message = (String) payload.get("message");
      Integer rating = (Integer) payload.get("rating");
      int userid = AuthUtils.getUserId();

      if (shopid == null || message == null || rating == null) {
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
    int ratingOwnerId = ratingRepository.getUserIdByRatingId(id);
    if (!AuthUtils.isUserAdmin() && !ratingService.isUserRatingOwner(ratingOwnerId)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "You do not have permission to edit this rating"));
    }

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
    int ratingOwnerId = ratingRepository.getUserIdByRatingId(id);
    if (!AuthUtils.isUserAdmin() && !ratingService.isUserRatingOwner(ratingOwnerId)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "You do not have permission to delete this rating"));
    }

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
