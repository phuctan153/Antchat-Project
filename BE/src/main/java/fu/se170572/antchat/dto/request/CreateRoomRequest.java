package fu.se170572.antchat.dto.request;

import fu.se170572.antchat.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String name;
    private Room.RoomType type;
    private Long targetUserId;
    private List<Long> invitedUserIds;
}
