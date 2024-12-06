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

  public List<Message> getAllMessages(int lastId, int pageSize) {
    String query = "SELECT * FROM Messages WHERE id > ? ORDER BY id LIMIT ?";
    return jdbcTemplate.query(query, new Object[] {lastId, pageSize}, messageMapper);
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

  public int getSenderIdByMessageId(int id) {
    String query = "SELECT senderid FROM Messages WHERE id = ?";
    return jdbcTemplate.queryForObject(query, Integer.class, id);
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
