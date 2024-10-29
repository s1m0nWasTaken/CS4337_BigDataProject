package CS4337.Project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
class ChatParticipant {
  private Integer chatid;
  private Integer userid1;
  private Integer userid2;
}
