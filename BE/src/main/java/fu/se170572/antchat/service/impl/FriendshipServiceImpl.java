package fu.se170572.antchat.service.impl;

import fu.se170572.antchat.dto.request.CreateRoomRequest;
import fu.se170572.antchat.entity.Friendship;
import fu.se170572.antchat.entity.Room;
import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.repository.FriendshipRepository;
import fu.se170572.antchat.repository.UserRepository;
import fu.se170572.antchat.service.FriendshipService;
import fu.se170572.antchat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;

    // 1. Gửi lời mời kết bạn
    @Transactional
    public String sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Bạn không thể tự kết bạn với chính mình.");
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Không tìm thấy Sender"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Không tìm thấy Receiver"));

        Optional<Friendship> existingRelation = friendshipRepository.findRelationBetween(senderId, receiverId);

        if (existingRelation.isPresent()) {
            Friendship relation = existingRelation.get();
            if (relation.getStatus() == Friendship.FriendshipStatus.ACCEPTED) return "Hai người đã là bạn bè.";
            if (relation.getStatus() == Friendship.FriendshipStatus.PENDING) return "Lời mời đã được gửi trước đó.";
            if (relation.getStatus() == Friendship.FriendshipStatus.BLOCKED) throw new RuntimeException("Không thể gửi lời mời.");
        }

        // Tạo lời mời mới
        Friendship newRequest = Friendship.builder()
                .user(sender)
                .friend(receiver)
                .status(Friendship.FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        friendshipRepository.save(newRequest);
        return "Đã gửi lời mời kết bạn thành công.";
    }

    // 2. Chấp nhận lời mời kết bạn VÀ Tự động tạo phòng
    @Transactional
    public String acceptFriendRequest(Long receiverId, Long senderId) {
        // 1. Tìm bản ghi kết bạn
        Friendship request = friendshipRepository.findRelationBetween(senderId, receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn."));

        if (request.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
            return "Hai người đã là bạn bè.";
        }

        // 2. Đổi trạng thái thành ACCEPTED
        request.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());
        friendshipRepository.save(request);

        // 3. LOGIC MỚI: TỰ ĐỘNG TẠO PHÒNG CHAT DIRECT
        try {
            CreateRoomRequest createRoomRequest = new CreateRoomRequest();
            createRoomRequest.setType(Room.RoomType.DIRECT);
            createRoomRequest.setTargetUserId(senderId);
            roomService.createRoom(createRoomRequest, receiverId);

        } catch (Exception e) {
            System.err.println("Lỗi khi tự động tạo phòng chat: " + e.getMessage());
        }

        return "Đã chấp nhận kết bạn và tạo phòng chat thành công.";
    }

    // 3. Từ chối / Hủy kết bạn
    @Transactional
    public String rejectOrRemoveFriend(Long userId1, Long userId2) {
        Friendship relation = friendshipRepository.findRelationBetween(userId1, userId2)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mối quan hệ này."));

        friendshipRepository.delete(relation);
        return "Đã xóa mối quan hệ.";
    }

    // 4. Lấy danh sách bạn bè
    public List<User> getMyFriends(Long userId) {
        return friendshipRepository.findAcceptedFriendsByUserId(userId);
    }

    // 5. Lấy danh sách lời mời chờ xác nhận
    public List<User> getPendingRequests(Long userId) {
        return friendshipRepository.findPendingRequestsByUserId(userId);
    }
}
