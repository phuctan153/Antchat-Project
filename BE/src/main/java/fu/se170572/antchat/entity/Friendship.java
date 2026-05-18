package fu.se170572.antchat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "friend_id"}) // Tránh trùng lặp lời mời giữa 2 người
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người gửi lời mời kết bạn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend; // Người nhận lời mời

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum FriendshipStatus {
        PENDING,   // Đang chờ chấp nhận (User A gửi, User B chưa đồng ý)
        ACCEPTED,  // Đã là bạn bè (Đủ điều kiện lập phòng DIRECT hoặc mời vào GROUP)
        BLOCKED    // Đã chặn nhau
    }
}
