package fu.se170572.antchat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRESENCE_KEY_PREFIX = "user:presence:";

    public void markOnline(Long userId) {
        // Lưu trạng thái ONLINE trong 5 phút. Nếu user còn kết nối, ta sẽ có cơ chế gia hạn sau.
        redisTemplate.opsForValue().set(PRESENCE_KEY_PREFIX + userId, "ONLINE", 5, TimeUnit.MINUTES);
    }

    public void markOffline(Long userId) {
        redisTemplate.delete(PRESENCE_KEY_PREFIX + userId);
    }

    public void renewOnline(Long userId) {
        redisTemplate.expire(PRESENCE_KEY_PREFIX + userId, 5, TimeUnit.MINUTES);
    }

    public boolean isUserOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY_PREFIX + userId));
    }
}
