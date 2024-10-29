package CS4337.Project;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Message {
  private int id;
  private int chatid;
  private int senderid;
  private LocalDateTime createdAt;
  private String content;
}
