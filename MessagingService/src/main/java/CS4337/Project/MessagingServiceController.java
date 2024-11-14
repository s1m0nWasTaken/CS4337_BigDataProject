package CS4337.Project;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessagingServiceController {

  private final ChatParticipantRepository chatParticipantRepository;
  private final MessageRepository messageRepository;

  @Autowired
  public MessagingServiceController(
      ChatParticipantRepository chatParticipantRepository,
      MessageRepository messageRepository,
      JdbcTemplate jdbcTemplate) {
    this.chatParticipantRepository = chatParticipantRepository;
    this.messageRepository = messageRepository;
  }

  @GetMapping("/ChatParticipants")
  public ResponseEntity<Map<String, Object>> getChatParticipants() {
    try {
      List<ChatParticipant> participants = chatParticipantRepository.getAllChatParticipant();
      return ResponseEntity.ok(Map.of("success", participants));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Database error: " + e.getMessage()));
    }
  }

  @PostMapping("/ChatParticipants")
  public ResponseEntity<Map<String, Object>> createChat(@RequestBody Map<String, Integer> users) {
    try {
      if (users == null || !users.containsKey("userid1") || !users.containsKey("userid2")) {
        return ResponseEntity.badRequest().body(Map.of("error", "User IDs are required."));
      }

      Integer userid1 = users.get("userid1");
      Integer userid2 = users.get("userid2");

      if (userid1 == null || userid2 == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "User IDs cannot be null."));
      }

      int result = chatParticipantRepository.addChatParticipant(userid1, userid2);
      return ResponseEntity.ok(Map.of("success", result));

    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Database error: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
  }

  @DeleteMapping("/ChatParticipants/{chatid}")
  public ResponseEntity<Map<String, Object>> removeChat(@PathVariable("chatid") int chatid) {
    try {

      int rowsAffected = chatParticipantRepository.delChatParticipant(chatid);
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

  @GetMapping("/Messages")
  public ResponseEntity<Map<String, Object>> getMessages() {
    try {
      List<Message> messages = messageRepository.getAllMessages();
      return ResponseEntity.ok(Map.of("success", messages));
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Database error: " + e.getMessage()));
    }
  }

  @PostMapping("/Messages")
  public ResponseEntity<Map<String, Object>> sendMessage(
      @RequestBody Map<String, Object> messageDetails) {
    try {
      Integer chatid = (Integer) messageDetails.get("chatid");
      Integer senderid = (Integer) messageDetails.get("senderid");
      String content = (String) messageDetails.get("content");

      if (chatid == null || senderid == null || content == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Chat ID and Sender ID cannot be null."));
      }

      int result = messageRepository.addMessage(chatid, senderid, content);
      return ResponseEntity.ok(
          Map.of("success", "Message sent successfully", "message_id", result));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
  }

  @DeleteMapping("/Messages/{chatid}")
  public ResponseEntity<Map<String, Object>> removeMessage(@PathVariable int chatid) {
    try {
      int result = messageRepository.delMessage(chatid);
      if (result > 0) {
        return ResponseEntity.ok(
            Map.of("success", true, "message", "Message deleted successfully."));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", "Message not found."));
      }
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("success", false, "error", e.getMessage()));
    }
  }

  @PatchMapping("/Messages/{id}")
  public ResponseEntity<Map<String, Object>> editMessage(
      @PathVariable int id, @RequestParam String content) {
    try {
      int result = messageRepository.editMessage(id, content);
      if (result > 0) {
        return ResponseEntity.ok(
            Map.of("success", true, "message", "Message updated successfully."));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", "Message not found."));
      }
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Database error: " + e.getMessage()));
    }
  }
}
