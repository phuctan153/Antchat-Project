package fu.se170572.antchat.controller;

import fu.se170572.antchat.dto.request.ChatMessageRequest;
import fu.se170572.antchat.dto.request.ReadReceiptPayload;
import fu.se170572.antchat.dto.request.TypingEventPayload;
import fu.se170572.antchat.dto.response.ChatMessageResponse;
import fu.se170572.antchat.redis.RedisMessagePublisher;
import fu.se170572.antchat.security.CustomUserDetails;
import fu.se170572.antchat.service.MessageService;
import fu.se170572.antchat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final RedisMessagePublisher redisMessagePublisher;
    private final PresenceService presenceService;

    @GetMapping("/api/messages/{roomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessageResponse> messages = messageService.getChatHistory(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest chatMessageRequest, Principal principal) {

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

        presenceService.renewOnline(userDetails.getId());

        ChatMessageResponse savedMessage = messageService.saveMessage(
                chatMessageRequest,
                userDetails.getId(),
                userDetails.getUsername()
        );

        redisMessagePublisher.publish(savedMessage);
    }

    @GetMapping("/api/messages/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long roomId, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

        long unreadCount = messageService.getUnreadCount(roomId, userDetails.getId());
        return ResponseEntity.ok(unreadCount);
    }

    @PutMapping("/api/messages/rooms/{roomId}/read")
    public ResponseEntity<String> markRoomMessagesAsRead(@PathVariable Long roomId, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

        messageService.markMessagesAsRead(roomId, userDetails.getId());

        ReadReceiptPayload readPayload = new ReadReceiptPayload(roomId, userDetails.getId(), userDetails.getUsername());
        redisMessagePublisher.publishReadReceipt(readPayload);

        return ResponseEntity.ok("Đã đánh dấu đọc toàn bộ tin nhắn");
    }

    @GetMapping("/api/users/{userId}/status")
    public ResponseEntity<String> getUserStatus(@PathVariable Long userId) {
        boolean isOnline = presenceService.isUserOnline(userId);
        return ResponseEntity.ok(isOnline ? "ONLINE" : "OFFLINE");
    }

    @MessageMapping("/chat.typing")
    public void handleTypingEvent(Principal principal, @Payload Long roomId) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

        presenceService.renewOnline(userDetails.getId());

        TypingEventPayload typingEvent = new TypingEventPayload(roomId, userDetails.getUsername(), true);

        redisMessagePublisher.publishTypingEvent(typingEvent);
    }

    @MessageMapping("/chat.ping")
    public void keepAlive(Principal principal) {
        if (principal == null) return;

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

        presenceService.renewOnline(userDetails.getId());
    }
}
