package CS4337.Project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import CS4337.Project.Shared.Security.AuthUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class MessagingServiceTest {
  private static final int SENDER_ID = 1;

  @Mock private ChatParticipantRepository chatParticipantRepository;

  @Mock private MessageRepository messageRepository;

  @InjectMocks private MessagingServiceController messagingService;

  private MockedStatic<AuthUtils> mockedAuthUtils;

  @BeforeEach
  public void setUp() {
    mockedAuthUtils = mockStatic(AuthUtils.class);
    when(AuthUtils.getUserId()).thenReturn(SENDER_ID);
    when(AuthUtils.isUserAdmin()).thenReturn(true);
  }

  @AfterEach
  public void tearDown() {
    if (mockedAuthUtils != null) {
      mockedAuthUtils.close();
    }
  }

  @Test
  public void testSendMessageSuccess() {
    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("chatid", 1);
    messageDetails.put("senderid", SENDER_ID);
    messageDetails.put("content", "Hello!");

    when(messageRepository.addMessage(1, 1, "Hello!")).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Message sent successfully", response.getBody().get("success"));
    assertEquals(1, response.getBody().get("message_id"));
  }

  @Test
  public void testSendMessageMissingChatId() {
    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("senderid", SENDER_ID);
    messageDetails.put("content", "Hello!");

    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Chat ID and Sender ID cannot be null.", response.getBody().get("error"));
  }

  @Test
  public void testGetChatParticipantsSuccess() {
    List<ChatParticipant> participants =
        Arrays.asList(new ChatParticipant(1, 1, 2), new ChatParticipant(2, 3, 4));
    when(chatParticipantRepository.getAllChatParticipant(0, 50)).thenReturn(participants);

    ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants(0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(participants, response.getBody().get("success"));
  }

  @Test
  public void testGetChatParticipantsDatabaseError() {
    when(chatParticipantRepository.getAllChatParticipant(0, 50))
        .thenThrow(new DataAccessException("Database error") {});

    ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants(0, 50);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Database error: Database error", response.getBody().get("error"));
  }

  @Test
  public void testRemoveChatSuccess() {
    int chatId = 1;
    when(chatParticipantRepository.delChatParticipant(chatId)).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(chatId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Chat participant removed successfully.", response.getBody().get("message"));
  }

  @Test
  public void testRemoveChatNotFound() {
    int chatId = 1;
    when(chatParticipantRepository.delChatParticipant(chatId)).thenReturn(0);

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(chatId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Chat participant not found.", response.getBody().get("message"));
    assertFalse((Boolean) response.getBody().get("success"));
  }

  @Test
  public void testGetChatParticipantsWhenNoneExist() {

    when(chatParticipantRepository.getAllChatParticipant(0, 50))
        .thenReturn(Collections.emptyList());

    ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants(0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Collections.emptyList(), response.getBody().get("success"));
  }

  @Test
  public void testSendMessageInvalidChatId() {

    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("chatid", null);
    messageDetails.put("senderid", SENDER_ID);
    messageDetails.put("content", "Hello!");

    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Chat ID and Sender ID cannot be null.", response.getBody().get("error"));
  }

  @Test
  public void testSendMessageMissingSenderId() {

    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("chatid", 1);
    messageDetails.put("content", "Hello!");

    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Chat ID and Sender ID cannot be null.", response.getBody().get("error"));
  }

  @Test
  public void testCreateChatSuccess() {
    Map<String, Integer> users = new HashMap<>();
    users.put("userid1", SENDER_ID);
    users.put("userid2", 2);

    when(chatParticipantRepository.addChatParticipant(SENDER_ID, 2)).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = messagingService.createChat(users);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("success"));
  }

  @Test
  public void testCreateChatUnauthorized() {
    when(AuthUtils.getUserId()).thenReturn(3);

    Map<String, Integer> users = new HashMap<>();
    users.put("userid1", 1);
    users.put("userid2", 2);

    ResponseEntity<Map<String, Object>> response = messagingService.createChat(users);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("You do not have permission to create this chat", response.getBody().get("error"));
  }

  @Test
  public void testCreateChatMissingUsers() {
    ResponseEntity<Map<String, Object>> response = messagingService.createChat(null);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("User IDs are required.", response.getBody().get("error"));
  }

  @Test
  public void testEditMessageSuccess() {
    int messageId = 1;
    String newContent = "Updated content";

    when(messageRepository.getSenderIdByMessageId(messageId)).thenReturn(SENDER_ID);
    when(messageRepository.editMessage(messageId, newContent)).thenReturn(1);

    ResponseEntity<Map<String, Object>> response =
        messagingService.editMessage(messageId, newContent);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("success"));
    assertEquals("Message updated successfully.", response.getBody().get("message"));
  }

  @Test
  public void testEditMessageUnauthorized() {
    int messageId = 1;
    String newContent = "Updated content";

    when(messageRepository.getSenderIdByMessageId(messageId)).thenReturn(2); // Different user

    ResponseEntity<Map<String, Object>> response =
        messagingService.editMessage(messageId, newContent);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals(
        "You do not have permission to edit this message", response.getBody().get("error"));
  }

  @Test
  public void testEditMessageNotFound() {
    int messageId = 1;
    String newContent = "Updated content";

    when(messageRepository.getSenderIdByMessageId(messageId)).thenReturn(SENDER_ID);
    when(messageRepository.editMessage(messageId, newContent)).thenReturn(0);

    ResponseEntity<Map<String, Object>> response =
        messagingService.editMessage(messageId, newContent);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(false, response.getBody().get("success"));
    assertEquals("Message not found.", response.getBody().get("message"));
  }

  @Test
  public void testRemoveMessageUnauthorized() {
    when(AuthUtils.isUserAdmin()).thenReturn(false);

    ResponseEntity<Map<String, Object>> response = messagingService.removeMessage(1);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals(
        "You do not have permission to delete this chat's messages",
        response.getBody().get("error"));
  }

  @Test
  public void testRemoveMessageDatabaseError() {
    when(messageRepository.delMessage(1)).thenThrow(new DataAccessException("Database error") {});

    ResponseEntity<Map<String, Object>> response = messagingService.removeMessage(1);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(false, response.getBody().get("success"));
    assertTrue(response.getBody().get("error").toString().contains("Database error"));
  }

  @Test
  public void testGetMessagesSuccess() {
    List<Message> messages =
        Arrays.asList(
            Message.builder().id(1).chatid(1).senderid(SENDER_ID).content("Hello").build(),
            Message.builder().id(2).chatid(1).senderid(2).content("Hi").build());

    when(messageRepository.getAllMessages(0, 50)).thenReturn(messages);

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(messages, response.getBody().get("success"));
  }

  @Test
  public void testGetMessagesEmptyResult() {
    when(messageRepository.getAllMessages(0, 50)).thenReturn(Collections.emptyList());

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(0, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Collections.emptyList(), response.getBody().get("success"));
  }

  @Test
  public void testGetMessagesPageSizeExceedsLimit() {
    List<Message> messages =
        Arrays.asList(
            Message.builder().id(1).chatid(1).senderid(SENDER_ID).content("Hello").build());

    when(messageRepository.getAllMessages(0, 50)).thenReturn(messages);

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(0, 100);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(messages, response.getBody().get("success"));
  }

  @Test
  public void testGetMessagesDatabaseError() {
    when(messageRepository.getAllMessages(0, 50))
        .thenThrow(new DataAccessException("Database connection failed") {});

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(0, 50);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Database error: Database connection failed", response.getBody().get("error"));
  }

  @Test
  public void testRemoveMessageSuccess() {
    int chatId = 1;
    when(messageRepository.delMessage(chatId)).thenReturn(1);

    ResponseEntity<Map<String, Object>> response = messagingService.removeMessage(chatId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("success"));
    assertEquals("Message deleted successfully.", response.getBody().get("message"));
  }

  @Test
  public void testRemoveMessageNotFound() {
    int chatId = 999;
    when(messageRepository.delMessage(chatId)).thenReturn(0);

    ResponseEntity<Map<String, Object>> response = messagingService.removeMessage(chatId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(false, response.getBody().get("success"));
    assertEquals("Message not found.", response.getBody().get("message"));
  }

  @Test
  public void testRemoveMessageMultipleMessages() {
    int chatId = 1;
    when(messageRepository.delMessage(chatId)).thenReturn(5);

    ResponseEntity<Map<String, Object>> response = messagingService.removeMessage(chatId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("success"));
    assertEquals("Message deleted successfully.", response.getBody().get("message"));
  }

  @Test
  public void testGetMessagesWithNegativeLastId() {
    List<Message> messages = Collections.emptyList();
    when(messageRepository.getAllMessages(-1, 50)).thenReturn(messages);

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(-1, 50);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(messages, response.getBody().get("success"));
  }

  @Test
  public void testGetMessagesWithNegativePageSize() {
    List<Message> messages = Collections.emptyList();
    when(messageRepository.getAllMessages(0, -1)).thenReturn(messages);

    ResponseEntity<Map<String, Object>> response = messagingService.getMessages(0, -1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(messages, response.getBody().get("success"));
  }

  @Test
  public void testRemoveChatDatabaseError() {
    when(chatParticipantRepository.delChatParticipant(1))
        .thenThrow(new DataAccessException("Database error") {});

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(1);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(false, response.getBody().get("success"));
    assertEquals("Database error", response.getBody().get("error"));
  }

  @Test
  public void testRemoveChatUnauthorizedNonAdmin() {
    when(AuthUtils.isUserAdmin()).thenReturn(false);

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(1);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("You do not have permission to delete this chat", response.getBody().get("error"));
  }

  @Test
  public void testRemoveChatWithNegativeId() {
    when(chatParticipantRepository.delChatParticipant(-1)).thenReturn(0);

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(-1);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(false, response.getBody().get("success"));
    assertEquals("Chat participant not found.", response.getBody().get("message"));
  }

  @Test
  public void testRemoveChatMultipleParticipants() {
    when(chatParticipantRepository.delChatParticipant(1)).thenReturn(2);

    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("success"));
    assertEquals("Chat participant removed successfully.", response.getBody().get("message"));
  }
}
