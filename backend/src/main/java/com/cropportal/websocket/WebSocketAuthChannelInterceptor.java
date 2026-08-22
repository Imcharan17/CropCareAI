package com.cropportal.websocket;

import com.cropportal.security.CurrentUserDetailsService;
import com.cropportal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final CurrentUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new IllegalArgumentException("WebSocket authentication required");
            }
            String token = header.substring(7);
            String email = jwtService.subject(token);
            if (!jwtService.valid(token)) {
                throw new IllegalArgumentException("Invalid WebSocket token");
            }
            var details = userDetailsService.loadUserByUsername(email);
            accessor.setUser(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        }
        return message;
    }
}
