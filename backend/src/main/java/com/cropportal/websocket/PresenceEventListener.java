package com.cropportal.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class PresenceEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void connected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.debug("WebSocket connected {}", accessor.getSessionId());
        messagingTemplate.convertAndSend("/topic/presence", java.util.Map.of("sessionId", accessor.getSessionId(), "status", "ONLINE"));
    }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        log.debug("WebSocket disconnected {}", event.getSessionId());
        messagingTemplate.convertAndSend("/topic/presence", java.util.Map.of("sessionId", event.getSessionId(), "status", "OFFLINE"));
    }
}
