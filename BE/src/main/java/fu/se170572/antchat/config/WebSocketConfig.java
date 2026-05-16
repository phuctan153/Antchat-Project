package fu.se170572.antchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Mở một cổng tên là "/ws" để Client (React/Vue/HTML) kết nối vào.
        // setAllowedOriginPatterns("*") cho phép mọi domain kết nối (sau này có thể chặn lại cho bảo mật).
        // withSockJS() là phương án dự phòng nếu trình duyệt cũ không hỗ trợ WebSocket thuần.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Cấu hình tiền tố cho các "Kênh" mà Server sẽ gửi tin nhắn VỀ cho Client
        // "/topic": Dùng để gửi tin nhắn cho NHIỀU người (VD: Chat Group).
        // "/queue": Dùng để gửi tin nhắn cho MỘT người cụ thể (VD: Chat Direct 1-1).
        registry.enableSimpleBroker("/topic", "/queue");

        // 3. Cấu hình tiền tố cho các tin nhắn từ Client gửi LÊN Server.
        // Bất kỳ tin nhắn nào bắt đầu bằng "/app" sẽ được đưa vào các @MessageMapping trong Controller để xử lý (lưu DB, kiểm tra logic...).
        registry.setApplicationDestinationPrefixes("/app");

        // 4. (Tùy chọn) Tiền tố để định tuyến tin nhắn đến một user cụ thể
        registry.setUserDestinationPrefix("/user");
    }
}
