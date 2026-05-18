package fu.se170572.antchat.repository;

import fu.se170572.antchat.entity.Friendship;
import fu.se170572.antchat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user.id = :uid1 AND f.friend.id = :uid2) OR " +
            "(f.user.id = :uid2 AND f.friend.id = :uid1)")
    Optional<Friendship> findRelationBetween(@Param("uid1") Long userId1, @Param("uid2") Long userId2);

    // 2. Lấy danh sách người gửi lời mời kết bạn đến user hiện tại (PENDING)
    @Query("SELECT f.user FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<User> findPendingRequestsByUserId(@Param("userId") Long userId);

    // 3. Lấy danh sách bạn bè (ACCEPTED) - Gộp cả 2 trường hợp user là người gửi hoặc người nhận
    @Query("SELECT u FROM User u WHERE u.id IN (" +
            "SELECT f.friend.id FROM Friendship f WHERE f.user.id = :userId AND f.status = 'ACCEPTED' " +
            "UNION " +
            "SELECT f.user.id FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'ACCEPTED')")
    List<User> findAcceptedFriendsByUserId(@Param("userId") Long userId);
}
