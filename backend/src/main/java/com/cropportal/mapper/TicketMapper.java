package com.cropportal.mapper;

import com.cropportal.dto.TicketResponse;
import com.cropportal.entity.Ticket;

public final class TicketMapper {
    private TicketMapper() {
    }

    public static TicketResponse toResponse(Ticket ticket) {
        String doctorName = ticket.getDoctor() == null ? "Unassigned" : ticket.getDoctor().getUser().getFullName();
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getFarmer().getUser().getFullName(),
                doctorName, ticket.getStatus(), ticket.getPriority(), ticket.getTreatmentRecommendation(), ticket.getCreatedAt());
    }
}
