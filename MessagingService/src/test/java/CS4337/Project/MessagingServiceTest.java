package CS4337.Project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class MessagingServiceTest {

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessagingService messagingService;

    @Test
    public void testSendMessageSuccess() {
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("chatid", 1);
        messageDetails.put("senderid", 1);
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
        messageDetails.put("senderid", 1);
        messageDetails.put("content", "Hello!");

        ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Chat ID and Sender ID cannot be null.", response.getBody().get("error"));
    }

    @Test
    public void testGetChatParticipantsSuccess() {
        List<ChatParticipant> participants = Arrays.asList(
            new ChatParticipant(1, 1, 2),
            new ChatParticipant(2, 3, 4)
        );
        when(chatParticipantRepository.getAllChatParticipant()).thenReturn(participants);

        ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(participants, response.getBody().get("success"));
    }

    @Test
    public void testGetChatParticipantsDatabaseError() {
        when(chatParticipantRepository.getAllChatParticipant())
            .thenThrow(new DataAccessException("Database error") {});

        ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants();

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
        assertEquals("Chat participant not found.", response.getBody().get("error"));
    }

    @Test
public void testGetChatParticipantsUnexpectedException() {
    // Arrange
    when(chatParticipantRepository.getAllChatParticipant()).thenThrow(new RuntimeException("Unexpected error"));

    // Act
    ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants();

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Unexpected error occurred.", response.getBody().get("error"));
}

@Test
public void testGetChatParticipantsWhenNoneExist() {
    // Arrange
    when(chatParticipantRepository.getAllChatParticipant()).thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<Map<String, Object>> response = messagingService.getChatParticipants();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Collections.emptyList(), response.getBody().get("success"));
}

@Test
public void testRemoveChatWhenNoParticipants() {
    // Arrange
    int chatId = 1;
    when(chatParticipantRepository.delChatParticipant(chatId)).thenReturn(0); // No participants to remove

    // Act
    ResponseEntity<Map<String, Object>> response = messagingService.removeChat(chatId);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Chat participant not found.", response.getBody().get("error"));
}

@Test
public void testSendMessageInvalidChatId() {
    // Arrange
    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("chatid", -1); // Invalid chat ID
    messageDetails.put("senderid", 1);
    messageDetails.put("content", "Hello!");

    // Act
    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid chat ID.", response.getBody().get("error"));
}

@Test
public void testSendMessageMissingSenderId() {
    // Arrange
    Map<String, Object> messageDetails = new HashMap<>();
    messageDetails.put("chatid", 1);
    messageDetails.put("content", "Hello!");

    // Act
    ResponseEntity<Map<String, Object>> response = messagingService.sendMessage(messageDetails);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Chat ID and Sender ID cannot be null.", response.getBody().get("error"));
}



}
