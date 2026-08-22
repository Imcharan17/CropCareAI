package com.cropportal.dto;

import com.cropportal.entity.SeverityLevel;

public record DiseasePredictionOption(
        String diseaseName,
        Double confidenceScore,
        SeverityLevel severityLevel,
        String affectedArea
) {
}
