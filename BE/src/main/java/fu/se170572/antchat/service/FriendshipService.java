package fu.se170572.antchat.service;

import fu.se170572.antchat.entity.User;

import java.util.List;

public interface FriendshipService {
    String sendFriendRequest(Long senderId, Long receiverId);
    String acceptFriendRequest(Long receiverId, Long senderId);
    String rejectOrRemoveFriend(Long userId1, Long userId2);
    List<User> getMyFriends(Long userId);
    List<User> getPendingRequests(Long userId);
}
