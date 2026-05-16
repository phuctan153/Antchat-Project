package fu.se170572.antchat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingEventPayload {
    private Long roomId;
    private String username;
    private boolean isTyping;
}
