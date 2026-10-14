package com.cropportal.service.impl;

import com.cropportal.ai.TicketAiSupportService;
import com.cropportal.dto.*;
import com.cropportal.entity.*;
import com.cropportal.exception.ResourceNotFoundException;
import com.cropportal.repository.*;
import com.cropportal.service.TicketService;
import com.cropportal.exception.BadRequestException;
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
    private final DoctorRepository doctorRepository;
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (hasRole(user, RoleName.ROLE_ADMIN)) {
            return ticketRepository.findAll(pageable).map(this::toResponse);
        }
        if (hasRole(user, RoleName.ROLE_DOCTOR)) {
            return ticketRepository.findDoctorQueue(email,
                    List.of(TicketStatus.OPEN, TicketStatus.AI_ANALYZED), pageable).map(this::toResponse);
        }
        return ticketRepository.findByFarmerUserEmail(email, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public TicketResponse assignDoctor(Long ticketId, Long doctorId) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (ticket.getDoctor() != null && !ticket.getDoctor().getId().equals(doctorId)) {
            throw new BadRequestException("Ticket is already assigned to another doctor");
        }
        ticket.setDoctor(doctor);
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            ticket.setStatus(TicketStatus.UNDER_TREATMENT);
        }
        return toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse claim(Long ticketId, String doctorEmail) {
        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        if (ticket.getDoctor() != null && !ticket.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("This ticket has already been claimed by another doctor");
        }
        if (ticket.getStatus() != TicketStatus.OPEN && ticket.getStatus() != TicketStatus.AI_ANALYZED
                && ticket.getStatus() != TicketStatus.UNDER_TREATMENT) {
            throw new BadRequestException("This ticket is not available for treatment");
        }
        ticket.setDoctor(doctor);
        ticket.setStatus(TicketStatus.UNDER_TREATMENT);
        return toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long ticketId, String actorEmail, TicketStatus status, String recommendation) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean admin = hasRole(actor, RoleName.ROLE_ADMIN);
        if (!admin) {
            if (!hasRole(actor, RoleName.ROLE_DOCTOR)
                    || ticket.getDoctor() == null
                    || !ticket.getDoctor().getUser().getEmail().equals(actorEmail)) {
                throw new BadRequestException("Only the assigned doctor or an admin can update this ticket");
            }
            if (status != TicketStatus.UNDER_TREATMENT && status != TicketStatus.RESOLVED) {
                throw new BadRequestException("Doctors can move tickets to UNDER_TREATMENT or RESOLVED");
            }
        }
        TicketStatus nextStatus = status == null ? TicketStatus.RESOLVED : status;
        if (nextStatus == TicketStatus.RESOLVED
                && (recommendation == null || recommendation.isBlank())
                && (ticket.getTreatmentRecommendation() == null || ticket.getTreatmentRecommendation().isBlank())) {
            throw new BadRequestException("Add a treatment recommendation before resolving the ticket");
        }
        ticket.setStatus(nextStatus);
        if (recommendation != null && !recommendation.isBlank()) {
            ticket.setTreatmentRecommendation(recommendation);
        }
        return toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public MessageResponse addMessage(Long ticketId, String senderEmail, MessageRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        ensureCanView(ticket, sender);
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(request.message());
        MessageResponse response = toMessage(messageRepository.save(message));
        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, response);
        return response;
    }

    @Override
    public List<MessageResponse> messages(Long ticketId, String requesterEmail) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow();
        ensureCanView(ticket, requester);
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream().map(this::toMessage).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void authorizeTicketAccess(Long ticketId, String requesterEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow();
        ensureCanView(ticket, requester);
    }

    private TicketResponse toResponse(Ticket ticket) {
        String doctorName = ticket.getDoctor() == null ? "Unassigned" : ticket.getDoctor().getUser().getFullName();
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getFarmer().getUser().getFullName(), doctorName, ticket.getStatus(),
                ticket.getPriority(), ticket.getTreatmentRecommendation(), ticket.getCreatedAt());
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
    }

    private void ensureCanView(Ticket ticket, User user) {
        if (hasRole(user, RoleName.ROLE_ADMIN)
                || ticket.getFarmer().getUser().getId().equals(user.getId())
                || (ticket.getDoctor() != null && ticket.getDoctor().getUser().getId().equals(user.getId()))) {
            return;
        }
        throw new ResourceNotFoundException("Ticket not found");
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
