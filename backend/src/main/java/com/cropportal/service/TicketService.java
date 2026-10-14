package com.cropportal.service;

import com.cropportal.dto.*;
import com.cropportal.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {
    TicketResponse create(String farmerEmail, TicketRequest request);
    Page<TicketResponse> listForUser(String email, Pageable pageable);
    TicketResponse assignDoctor(Long ticketId, Long doctorId);
    TicketResponse claim(Long ticketId, String doctorEmail);
    TicketResponse updateStatus(Long ticketId, String actorEmail, TicketStatus status, String recommendation);
    MessageResponse addMessage(Long ticketId, String senderEmail, MessageRequest request);
    List<MessageResponse> messages(Long ticketId, String requesterEmail);
    void authorizeTicketAccess(Long ticketId, String requesterEmail);
}
