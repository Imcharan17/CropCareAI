package com.cropportal.dto;

import com.cropportal.entity.TicketStatus;

import java.time.Instant;

public record TicketResponse(Long id, String title, String farmerName, String doctorName, TicketStatus status,
                             String priority, String treatmentRecommendation, Instant createdAt) {
}
