package fu.se170572.antchat.repository;

import fu.se170572.antchat.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Transactional
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
}
