package fu.se170572.antchat.redis;

import fu.se170572.antchat.dto.request.ReadReceiptPayload;
import fu.se170572.antchat.dto.request.TypingEventPayload;
import fu.se170572.antchat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic;
    private final ChannelTopic typingTopic;
    private final ChannelTopic readReceiptTopic;

    public void publish(ChatMessageResponse message) {
        log.info("Publishing tin nhắn lên Redis topic {}: {}", chatTopic.getTopic(), message.getContent());
        redisTemplate.convertAndSend(chatTopic.getTopic(), message);
    }

    public void publishTypingEvent(TypingEventPayload payload) {
        redisTemplate.convertAndSend(typingTopic.getTopic(), payload);
    }

    public void publishReadReceipt(ReadReceiptPayload payload) {
        redisTemplate.convertAndSend(readReceiptTopic.getTopic(), payload);
    }
}
