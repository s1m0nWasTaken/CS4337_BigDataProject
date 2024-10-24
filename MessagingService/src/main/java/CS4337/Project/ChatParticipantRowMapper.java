package CS4337.Project;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

class ChatParticipantRowMapper implements RowMapper<ChatParticipant> {
  @Override
  public ChatParticipant mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    ChatParticipant participant = new ChatParticipant();
    participant.setChatId(rs.getInt("chatid"));
    participant.setUserId1(rs.getInt("userid1"));
    participant.setUserId2(rs.getInt("userid2"));
    return participant;
  }
}
