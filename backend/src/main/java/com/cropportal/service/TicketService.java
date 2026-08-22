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
    TicketResponse updateStatus(Long ticketId, TicketStatus status, String recommendation);
    MessageResponse addMessage(Long ticketId, String senderEmail, MessageRequest request);
    List<MessageResponse> messages(Long ticketId);
}
