package fu.se170572.antchat.service;

import fu.se170572.antchat.dto.request.CreateRoomRequest;
import fu.se170572.antchat.dto.response.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto createRoom(CreateRoomRequest request, Long creatorId);
    List<RoomDto> getAllRooms();
    void addMemberToRoom(Long roomId, Long userId);
    void removeMemberFromRoom(Long roomId, Long userId);
}
