package CS4337.Project;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class MessagingService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public static void main(String[] args) {
    SpringApplication.run(MessagingService.class, args);
  }

  @GetMapping("/ChatParticipants")
  public ResponseEntity<Map<String, Object>> getChatParticipants() {
    String sql = "SELECT * FROM ChatParticipants";
    List<ChatParticipant> participants = jdbcTemplate.query(sql, new ChatParticipantRowMapper());

    return ResponseEntity.ok(Map.of("success", participants));
  }

  @GetMapping("/Messages")
  public ResponseEntity<Map<String, Object>> getMessages() {
    String sql = "SELECT * FROM Messages";
    List<ChatParticipant> participants = jdbcTemplate.query(sql, new ChatParticipantRowMapper());

    return ResponseEntity.ok(Map.of("success", participants));
  }

  // @PostMapping("/Messages")
  // public Map<String, Object> sendMessage(@RequestBody User user) {
  // }

  // @DeleteMapping("/Messages")
  // public Map<String, Object> removeMessage() {
  // }

  // @PatchMapping("/Messages")
  // public Map<String, Object> editMessage() {
  // }

  @PostMapping("/ChatParticipants")
  public ResponseEntity<Map<String, Object>> createChat(@RequestBody Map<String, Integer> users) {
    try {
      // Validate input
      if (users == null || !users.containsKey("userid1") || !users.containsKey("userid2")) {
        return ResponseEntity.badRequest().body(Map.of("error", "User IDs are required."));
      }

      Integer userId1 = users.get("userid1");
      Integer userId2 = users.get("userid2");

      // Ensure user IDs are not null
      if (userId1 == null || userId2 == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "User IDs cannot be null."));
      }

      String sqlInsert = "INSERT INTO ChatParticipants (userid1, userid2) VALUES (?, ?);";

      jdbcTemplate.update(sqlInsert, userId1, userId2);
      return ResponseEntity.ok(Map.of("success", 1));

    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Database error: " + e.getMessage()));
    } catch (Exception e) {
      // Handle any other exceptions that might arise
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
  }

  @DeleteMapping("/ChatParticipants/{chatid}")
  public ResponseEntity<Map<String, Object>> removeChat(@PathVariable("chatid") int chatid) {
    try {
      // SQL statement to delete the chat participant by chatid
      String sqlDelete = "DELETE FROM ChatParticipants WHERE chatid = ?;";
      int rowsAffected = jdbcTemplate.update(sqlDelete, chatid);

      // Check if any rows were affected
      if (rowsAffected > 0) {
        return ResponseEntity.ok(
            Map.of("success", true, "message", "Chat participant removed successfully."));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", "Chat participant not found."));
      }

    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("success", false, "error", e.getMessage()));
    }
  }
}
