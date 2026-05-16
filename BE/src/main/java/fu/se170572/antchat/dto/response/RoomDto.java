package fu.se170572.antchat.dto.response;

import fu.se170572.antchat.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String name;
    private Room.RoomType type;
    private LocalDateTime createdAt;
}
