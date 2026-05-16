package fu.se170572.antchat.service;

import fu.se170572.antchat.dto.request.ChatMessageRequest;
import fu.se170572.antchat.dto.response.ChatMessageResponse;

import java.util.List;

public interface MessageService {
    ChatMessageResponse saveMessage(ChatMessageRequest request, Long senderId, String senderName);
    List<ChatMessageResponse> getChatHistory(Long roomId, int page, int size);
    long getUnreadCount(Long roomId, Long userId);
    void markMessagesAsRead(Long roomId, Long userId);
}
