package CS4337.Project;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public MessageRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public int addMessage(int chatid, int senderid, String content) {
    String query =
        "INSERT INTO Messages (chatid, senderid, content, createdAt) VALUES (?, ?, ?, Now())";
    return jdbcTemplate.update(query, chatid, senderid, content);
  }

  public List<Message> getAllMessages() {
    String query = "SELECT * FROM Messages";
    return jdbcTemplate.query(query, messageMapper);
  }

  public List<Message> getMessageByChatId(int chatid) {
    String query = "SELECT * FROM Messages WHERE chatid = ?";
    return jdbcTemplate.query(query, new Object[] {chatid}, messageMapper);
  }

  public int editMessage(int id, String content) {
    String query = "UPDATE Messages SET content = ? WHERE id = ?";
    return jdbcTemplate.update(query, content, id);
  }

  public int delMessage(int chatid) {
    String query = "DELETE FROM Messages WHERE chatid = ?";
    return jdbcTemplate.update(query, chatid);
  }

  private RowMapper<Message> messageMapper =
      (rs, rowNum) ->
          new Message(
              rs.getInt("id"),
              rs.getInt("chatid"),
              rs.getInt("senderid"),
              rs.getTimestamp("createdAt").toLocalDateTime(),
              rs.getString("content"));
}
