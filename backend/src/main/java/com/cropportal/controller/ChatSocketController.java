package com.cropportal.controller;

import com.cropportal.dto.MessageRequest;
import com.cropportal.dto.MessageResponse;
import com.cropportal.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {
    private final TicketService ticketService;

    @MessageMapping("/tickets/{ticketId}/chat")
    public MessageResponse chat(@DestinationVariable Long ticketId, MessageRequest request, Principal principal,
                                SimpMessageHeaderAccessor headers) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated WebSocket principal is required");
        }
        return ticketService.addMessage(ticketId, principal.getName(), request);
    }
}
