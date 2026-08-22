package com.cropportal.service.impl;

import com.cropportal.ai.TicketAiSupportService;
import com.cropportal.dto.*;
import com.cropportal.entity.*;
import com.cropportal.exception.ResourceNotFoundException;
import com.cropportal.repository.*;
import com.cropportal.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private static final String AI_SUPPORT_EMAIL = "ai-support@crop.ai";

    private final TicketRepository ticketRepository;
    private final FarmerRepository farmerRepository;
    private final DiseaseReportRepository diseaseReportRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketAiSupportService ticketAiSupportService;

    @Override
    @Transactional
    public TicketResponse create(String farmerEmail, TicketRequest request) {
        Farmer farmer = farmerRepository.findByUserEmail(farmerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found"));
        DiseaseReport diseaseReport = findOwnedDiseaseReport(farmer, request.diseaseReportId());
        Ticket ticket = new Ticket();
        ticket.setFarmer(farmer);
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority() == null ? "MEDIUM" : request.priority());
        ticket.setDiseaseReport(diseaseReport);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        saved.setTreatmentRecommendation(ticketAiSupportService.resolveTicket(farmer, request, diseaseReport));
        saved.setStatus(TicketStatus.AI_ANALYZED);
        saved = ticketRepository.save(saved);
        saveAiMessage(saved, saved.getTreatmentRecommendation());
        return toResponse(saved);
    }

    @Override
    public Page<TicketResponse> listForUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
            return ticketRepository.findAll(pageable).map(this::toResponse);
        }
        return ticketRepository.findByFarmerUserEmail(email, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public TicketResponse assignDoctor(Long ticketId, Long doctorId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.setStatus(TicketStatus.AI_ANALYZED);
        if (ticket.getTreatmentRecommendation() == null || ticket.getTreatmentRecommendation().isBlank()) {
            ticket.setTreatmentRecommendation("Resolved by AI Support. Doctor assignment is no longer used in this workflow.");
        }
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long ticketId, TicketStatus status, String recommendation) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.setStatus(status == null ? TicketStatus.RESOLVED : status);
        if (recommendation != null && !recommendation.isBlank()) {
            ticket.setTreatmentRecommendation(recommendation);
        }
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public MessageResponse addMessage(Long ticketId, String senderEmail, MessageRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(request.message());
        MessageResponse response = toMessage(messageRepository.save(message));
        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, response);
        return response;
    }

    @Override
    public List<MessageResponse> messages(Long ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream().map(this::toMessage).toList();
    }

    private TicketResponse toResponse(Ticket ticket) {
        String doctorName = "AI Support";
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getFarmer().getUser().getFullName(),
                doctorName, ticket.getStatus(), ticket.getPriority(), ticket.getTreatmentRecommendation(), ticket.getCreatedAt());
    }

    private MessageResponse toMessage(TicketMessage message) {
        return new MessageResponse(message.getId(), message.getTicket().getId(), message.getSender().getFullName(),
                message.getMessage(), message.isReadReceipt(), message.getCreatedAt());
    }

    private DiseaseReport findOwnedDiseaseReport(Farmer farmer, Long diseaseReportId) {
        if (diseaseReportId == null) return null;
        DiseaseReport report = diseaseReportRepository.findById(diseaseReportId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease report not found"));
        if (!report.getFarmer().getId().equals(farmer.getId())) {
            throw new ResourceNotFoundException("Disease report not found");
        }
        return report;
    }

    private void saveAiMessage(Ticket ticket, String recommendation) {
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(aiSupportUser());
        message.setMessage(recommendation);
        message.setReadReceipt(false);
        MessageResponse response = toMessage(messageRepository.save(message));
        messagingTemplate.convertAndSend("/topic/tickets/" + ticket.getId(), response);
    }

    private User aiSupportUser() {
        return userRepository.findByEmail(AI_SUPPORT_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setFullName("AI Support");
            user.setEmail(AI_SUPPORT_EMAIL);
            user.setPassword("SYSTEM_ACCOUNT_DISABLED");
            user.setRoles(java.util.Set.of(roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow()));
            return userRepository.save(user);
        });
    }
}
