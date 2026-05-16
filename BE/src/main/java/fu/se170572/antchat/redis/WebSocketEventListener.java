package fu.se170572.antchat.redis;

import fu.se170572.antchat.security.CustomUserDetails;
import fu.se170572.antchat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) accessor.getUser();
            CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

            presenceService.markOnline(userDetails.getId());
            log.info("User ID {} is ONLINE", userDetails.getId());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) accessor.getUser();
            CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();

            presenceService.markOffline(userDetails.getId());
            log.info("User ID {} is OFFLINE", userDetails.getId());
        }
    }

}
