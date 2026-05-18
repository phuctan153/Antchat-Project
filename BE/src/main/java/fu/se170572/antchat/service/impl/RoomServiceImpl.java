package fu.se170572.antchat.service.impl;

import fu.se170572.antchat.dto.request.CreateRoomRequest;
import fu.se170572.antchat.dto.response.RoomDto;
import fu.se170572.antchat.entity.Friendship;
import fu.se170572.antchat.entity.Room;
import fu.se170572.antchat.entity.RoomMember;
import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.repository.FriendshipRepository;
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
    private final FriendshipRepository friendshipRepository;

    @Override
    @Transactional
    public RoomDto createRoom(CreateRoomRequest request, Long creatorId) {
        // --- 1. TẦNG KIỂM TRA ĐIỀU KIỆN (VALIDATION) ---
        if (request.getType() == Room.RoomType.DIRECT) {
            Long targetId = request.getTargetUserId();
            if (targetId == null) throw new RuntimeException("Phòng DIRECT yêu cầu phải có targetUserId");

            Friendship friendship = friendshipRepository.findRelationBetween(creatorId, targetId)
                    .orElseThrow(() -> new RuntimeException("Hai người dùng chưa từng có tương tác kết bạn."));

            if (friendship.getStatus() != Friendship.FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Không thể tạo phòng chat trực tiếp vì hai người chưa là bạn bè.");
            }
        }
        else if (request.getType() == Room.RoomType.GROUP) {
            List<Long> invitedIds = request.getInvitedUserIds();
            if (invitedIds == null || invitedIds.isEmpty()) {
                throw new RuntimeException("Để tạo nhóm chat, bạn cần mời ít nhất 1 người bạn.");
            }

            // Kiểm tra từng người được mời xem có phải là bạn bè không
            for (Long friendId : invitedIds) {
                Friendship friendship = friendshipRepository.findRelationBetween(creatorId, friendId)
                        .orElseThrow(() -> new RuntimeException("Người dùng ID " + friendId + " không phải là bạn bè của bạn."));

                if (friendship.getStatus() != Friendship.FriendshipStatus.ACCEPTED) {
                    throw new RuntimeException("Người dùng ID " + friendId + " không phải là bạn bè của bạn. Không thể mời vào nhóm.");
                }
            }
        }

        // --- 2. TẦNG LƯU DỮ LIỆU (PERSISTENCE) ---
        Room room = Room.builder()
                .name(request.getName())
                .type(request.getType())
                .build();
        Room savedRoom = roomRepository.save(room);

        // Lưu Chủ phòng vào room_members
        User creator = userRepository.getReferenceById(creatorId);
        roomMemberRepository.save(RoomMember.builder()
                .id(new RoomMember.RoomMemberId(savedRoom.getId(), creatorId))
                .room(savedRoom)
                .user(creator)
                .build());

        // Lưu Thành viên cho phòng DIRECT
        if (savedRoom.getType() == Room.RoomType.DIRECT && request.getTargetUserId() != null) {
            User targetUser = userRepository.getReferenceById(request.getTargetUserId());
            roomMemberRepository.save(RoomMember.builder()
                    .id(new RoomMember.RoomMemberId(savedRoom.getId(), request.getTargetUserId()))
                    .room(savedRoom)
                    .user(targetUser)
                    .build());
        }
        // Lưu danh sách Thành viên cho phòng GROUP
        else if (savedRoom.getType() == Room.RoomType.GROUP && request.getInvitedUserIds() != null) {
            for (Long invitedId : request.getInvitedUserIds()) {
                User invitedUser = userRepository.getReferenceById(invitedId);
                roomMemberRepository.save(RoomMember.builder()
                        .id(new RoomMember.RoomMemberId(savedRoom.getId(), invitedId))
                        .room(savedRoom)
                        .user(invitedUser)
                        .build());
            }
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

        roomMember.setRoom(room);
        roomMember.setUser(user);
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
