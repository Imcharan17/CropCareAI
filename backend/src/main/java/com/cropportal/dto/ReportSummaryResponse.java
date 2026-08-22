package com.cropportal.dto;

import com.cropportal.entity.SeverityLevel;
import com.cropportal.entity.TicketStatus;

import java.time.Instant;

public record ReportSummaryResponse(
        Long reportId,
        String farmerName,
        String mobileNumber,
        String district,
        String cropName,
        String diseaseName,
        Double confidence,
        SeverityLevel severity,
        String location,
        Instant detectionDate,
        TicketStatus ticketStatus,
        String currentStatus,
        String treatmentSummary,
        String aiRecommendationSummary,
        String imageUrl
) {
}
