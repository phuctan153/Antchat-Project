package fu.se170572.antchat.service.impl;

import fu.se170572.antchat.dto.request.CreateRoomRequest;
import fu.se170572.antchat.dto.response.RoomDto;
import fu.se170572.antchat.entity.Room;
import fu.se170572.antchat.entity.RoomMember;
import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.repository.RoomMemberRepository;
import fu.se170572.antchat.repository.RoomRepository;
import fu.se170572.antchat.repository.UserRepository;
import fu.se170572.antchat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RoomDto createRoom(CreateRoomRequest request, Long creatorId) {
        Room room = Room.builder()
                .name(request.getName())
                .type(request.getType())
                .build();
        Room savedRoom = roomRepository.save(room);

        User creator = userRepository.getReferenceById(creatorId);

        roomMemberRepository.save(RoomMember.builder()
                .id(new RoomMember.RoomMemberId(savedRoom.getId(), creatorId))
                .room(savedRoom)
                .user(creator)
                .build());

        if (savedRoom.getType() == Room.RoomType.DIRECT && request.getTargetUserId() != null) {
            User targetUser = userRepository.getReferenceById(request.getTargetUserId());

            roomMemberRepository.save(RoomMember.builder()
                    .id(new RoomMember.RoomMemberId(savedRoom.getId(), request.getTargetUserId()))
                    .room(savedRoom)
                    .user(targetUser)
                    .build());
        }

        return mapToDto(savedRoom);
    }

    @Override
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private RoomDto mapToDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .type(room.getType())
                .createdAt(room.getCreatedAt())
                .build();
    }

    @Override
    public void addMemberToRoom(Long roomId, Long userId) {
        // 1. Kiểm tra Room và User có tồn tại không
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 2. Kiểm tra user đã ở trong phòng chưa
        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("Người dùng đã ở trong phòng này rồi!");
        }

        // 3. Thêm vào phòng
        RoomMember roomMember = new RoomMember();
        roomMember.setId(new RoomMember.RoomMemberId(roomId, userId));

        roomMember.setJoinedAt(LocalDateTime.now());

        roomMemberRepository.save(roomMember);
    }

    @Override
    public void removeMemberFromRoom(Long roomId, Long userId) {
        // Kiểm tra xem user có trong phòng không (tùy chọn)
        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("Người dùng không thuộc phòng chat này!");
        }

        // Xóa khỏi bảng room_members
        roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }
}
