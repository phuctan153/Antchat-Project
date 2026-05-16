package fu.se170572.antchat.repository;

import fu.se170572.antchat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRoomId(Long roomId, Pageable pageable);
    // 1. Đếm số tin nhắn chưa đọc (trạng thái SENT) của người khác gửi cho mình trong phòng
    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId AND m.senderId != :userId AND m.status = 'SENT'")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 2. Cập nhật trạng thái tin nhắn thành READ
    @Modifying
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.roomId = :roomId AND m.senderId != :userId AND m.status = 'SENT'")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
