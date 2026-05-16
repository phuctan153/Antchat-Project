package fu.se170572.antchat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import fu.se170572.antchat.dto.request.ReadReceiptPayload;
import fu.se170572.antchat.dto.request.TypingEventPayload;
import fu.se170572.antchat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonMessage = new String(message.getBody());
            String channel = new String(message.getChannel()); // Lấy tên kênh gửi đến

            // 1. Nếu là tin nhắn Chat bình thường
            if (channel.equals("chat_messages_topic")) {
                ChatMessageResponse chatMessage = objectMapper.readValue(jsonMessage, ChatMessageResponse.class);
                messagingTemplate.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessage);
            }
            // 2. Nếu là sự kiện "Đang gõ phím"
            else if (channel.equals("chat_typing_topic")) {
                TypingEventPayload typing = objectMapper.readValue(jsonMessage, TypingEventPayload.class);
                messagingTemplate.convertAndSend("/topic/room/" + typing.getRoomId() + "/typing", typing);
            }
            // 3. Nếu là sự kiện "Đã xem"
            else if (channel.equals("chat_read_topic")) {
                ReadReceiptPayload read = objectMapper.readValue(jsonMessage, ReadReceiptPayload.class);
                messagingTemplate.convertAndSend("/topic/room/" + read.getRoomId() + "/read", read);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn từ Redis: ", e);
        }
    }
}
