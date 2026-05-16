package fu.se170572.antchat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Người nhận thông báo

    @Column(name = "sender_id")
    private Long senderId; // Người gây ra thông báo (ví dụ: ai vừa nhắn tin)

    @Column(nullable = false)
    private String type; // Ví dụ: "NEW_MESSAGE", "FRIEND_REQUEST"

    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
