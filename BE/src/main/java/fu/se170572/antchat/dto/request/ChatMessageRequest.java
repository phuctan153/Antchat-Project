package fu.se170572.antchat.dto.request;

import fu.se170572.antchat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private String content;
    private MessageType type;
}
