package fu.se170572.antchat.controller;

import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.security.CustomUserDetails;
import fu.se170572.antchat.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // Helper method lấy ID của User đang đăng nhập
    private Long getUserIdFromPrincipal(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        return userDetails.getId();
    }

    // 1. API: Lấy danh sách bạn bè của tôi
    // GET /api/friends
    @GetMapping
    public ResponseEntity<?> getMyFriends(Principal principal) {
        Long myId = getUserIdFromPrincipal(principal);
        List<User> friends = friendshipService.getMyFriends(myId);
        return ResponseEntity.ok(friends);
    }

    // 2. API: Lấy danh sách lời mời đang chờ tôi xác nhận
    // GET /api/friends/pending
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        Long myId = getUserIdFromPrincipal(principal);
        List<User> pendingUsers = friendshipService.getPendingRequests(myId);
        return ResponseEntity.ok(pendingUsers);
    }

    // 3. API: Gửi lời mời kết bạn tới một người
    // POST /api/friends/request/{receiverId}
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long receiverId, Principal principal) {
        Long myId = getUserIdFromPrincipal(principal);
        String result = friendshipService.sendFriendRequest(myId, receiverId);

        // Cực kì Xịn: Bạn có thể dùng RedisMessagePublisher ở đây để bắn 1 event Real-time
        // thông báo cho receiverId biết có người vừa gửi lời mời kết bạn!

        return ResponseEntity.ok(result);
    }

    // 4. API: Chấp nhận kết bạn
    // PUT /api/friends/accept/{senderId}
    @PutMapping("/accept/{senderId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long senderId, Principal principal) {
        Long myId = getUserIdFromPrincipal(principal); // myId chính là receiverId
        String result = friendshipService.acceptFriendRequest(myId, senderId);
        return ResponseEntity.ok(result);
    }

    // 5. API: Hủy kết bạn hoặc Từ chối lời mời
    // DELETE /api/friends/remove/{targetUserId}
    @DeleteMapping("/remove/{targetUserId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long targetUserId, Principal principal) {
        Long myId = getUserIdFromPrincipal(principal);
        String result = friendshipService.rejectOrRemoveFriend(myId, targetUserId);
        return ResponseEntity.ok(result);
    }
}
