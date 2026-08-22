package com.cropportal.controller;

import com.cropportal.dto.*;
import com.cropportal.entity.TicketStatus;
import com.cropportal.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    public Page<TicketResponse> list(Authentication authentication, Pageable pageable) {
        return ticketService.listForUser(authentication.getName(), pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public TicketResponse create(Authentication authentication, @Valid @RequestBody TicketRequest request) {
        return ticketService.create(authentication.getName(), request);
    }

    @PatchMapping("/{ticketId}/assign/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponse assign(@PathVariable Long ticketId, @PathVariable Long doctorId) {
        return ticketService.assignDoctor(ticketId, doctorId);
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponse status(@PathVariable Long ticketId, @RequestParam TicketStatus status,
                                 @RequestParam(required = false) String recommendation) {
        return ticketService.updateStatus(ticketId, status, recommendation);
    }

    @GetMapping("/{ticketId}/messages")
    public List<MessageResponse> messages(@PathVariable Long ticketId) {
        return ticketService.messages(ticketId);
    }

    @PostMapping("/{ticketId}/messages")
    public MessageResponse send(Authentication authentication, @PathVariable Long ticketId,
                                @Valid @RequestBody MessageRequest request) {
        return ticketService.addMessage(ticketId, authentication.getName(), request);
    }
}
