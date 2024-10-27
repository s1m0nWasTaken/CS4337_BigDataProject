package CS4337.Project;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
class ChatParticipantRepository implements RowMapper<ChatParticipant> {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public ChatParticipantRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ChatParticipant mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    ChatParticipant participant =
        new ChatParticipant.ChatParticipantBuilder()
            .chatid(rs.getInt("chatid"))
            .userid1(rs.getInt("userid1"))
            .userid2(rs.getInt("userid2"))
            .build();
    return participant;
  }

  public int addChatParticipant(int userid1, int userid2) {
    String sqlInsert = "INSERT INTO ChatParticipants (userid1, userid2) VALUES (?, ?);";
    return jdbcTemplate.update(sqlInsert, userid1, userid2);
  }

  public List<ChatParticipant> getAllChatParticipant() {
    String sql = "SELECT * FROM ChatParticipants";
    return jdbcTemplate.query(sql, this);
  }

  public int delChatParticipant(int chatid) {
    String sqlDelete = "DELETE FROM ChatParticipants WHERE chatid = ?;";
    return jdbcTemplate.update(sqlDelete, chatid);
  }
}
