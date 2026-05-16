package fu.se170572.antchat.service.impl;

import fu.se170572.antchat.dto.request.ChatMessageRequest;
import fu.se170572.antchat.dto.response.ChatMessageResponse;
import fu.se170572.antchat.entity.Message;
import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.repository.MessageRepository;
import fu.se170572.antchat.repository.UserRepository;
import fu.se170572.antchat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest request, Long senderId, String senderName) {
        Message message = Message.builder()
                .roomId(request.getRoomId())
                .senderId(senderId)
                .content(request.getContent())
                .type(request.getType())
                .status(Message.MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMsg = messageRepository.save(message);

        return ChatMessageResponse.builder()
                .id(savedMsg.getId())
                .roomId(savedMsg.getRoomId())
                .senderId(senderId)
                .senderName(senderName)
                .content(savedMsg.getContent())
                .type(savedMsg.getType())
                .status(savedMsg.getStatus().name())
                .createdAt(savedMsg.getCreatedAt())
                .build();
    }

    @Override
    public List<ChatMessageResponse> getChatHistory(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository.findByRoomId(roomId, pageable);

        return messagePage.getContent().stream().map(msg -> {
            String senderName = userRepository.findById(msg.getSenderId())
                    .map(User::getUsername).orElse("Unknown");

            return ChatMessageResponse.builder()
                    .id(msg.getId())
                    .roomId(msg.getRoomId())
                    .senderId(msg.getSenderId())
                    .senderName(senderName)
                    .content(msg.getContent())
                    .status(msg.getStatus().name())
                    .createdAt(msg.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long roomId, Long userId) {
        return messageRepository.countUnreadMessages(roomId, userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        messageRepository.markMessagesAsRead(roomId, userId);
    }
}
