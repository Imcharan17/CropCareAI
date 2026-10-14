package com.cropportal.websocket;

import com.cropportal.security.CurrentUserDetailsService;
import com.cropportal.security.JwtService;
import com.cropportal.service.TicketService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private static final Pattern TICKET_TOPIC = Pattern.compile("^/topic/tickets/(\\d+)$");

    private final JwtService jwtService;
    private final CurrentUserDetailsService userDetailsService;
    private final ObjectProvider<TicketService> ticketServiceProvider;

    public WebSocketAuthChannelInterceptor(JwtService jwtService,
                                           CurrentUserDetailsService userDetailsService,
                                           ObjectProvider<TicketService> ticketServiceProvider) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.ticketServiceProvider = ticketServiceProvider;
    }

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
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Matcher ticketTopic = TICKET_TOPIC.matcher(accessor.getDestination() == null ? "" : accessor.getDestination());
            if (ticketTopic.matches()) {
                Principal principal = accessor.getUser();
                if (principal == null) {
                    throw new IllegalArgumentException("WebSocket authentication required");
                }
                ticketServiceProvider.getObject()
                        .authorizeTicketAccess(Long.parseLong(ticketTopic.group(1)), principal.getName());
            }
        }
        return message;
    }
}
