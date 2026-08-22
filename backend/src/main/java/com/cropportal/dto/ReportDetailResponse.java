package com.cropportal.dto;

import com.cropportal.entity.SeverityLevel;
import com.cropportal.entity.TicketStatus;

import java.time.Instant;
import java.util.List;

public record ReportDetailResponse(
        Long reportNumber,
        Instant generatedDate,
        FarmerDetails farmer,
        CropDetails crop,
        String uploadedImageUrl,
        String detectedDisease,
        String scientificName,
        Double confidenceScore,
        SeverityLevel severityLevel,
        String confidenceExplanation,
        List<String> symptoms,
        List<String> possibleCauses,
        String treatmentRecommendation,
        String preventiveMeasures,
        String recommendedFertilizers,
        String recommendedPesticides,
        String organicAlternatives,
        String estimatedRecoveryTime,
        String aiRecommendation,
        TicketStatus ticketStatus,
        List<String> historyOfActions
) {
    public record FarmerDetails(String name, String mobileNumber, String location, String district) {
    }

    public record CropDetails(String name, String season, String description) {
    }
}
