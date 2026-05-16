package fu.se170572.antchat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptPayload {
    private Long roomId;
    private Long userId;
    private String username;
}
