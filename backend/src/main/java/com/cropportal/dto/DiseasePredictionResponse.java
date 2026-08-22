package com.cropportal.dto;

import com.cropportal.entity.SeverityLevel;

import java.util.List;

public record DiseasePredictionResponse(
        Long reportId,
        String diseaseName,
        Double confidenceScore,
        String affectedArea,
        SeverityLevel severityLevel,
        String confidenceExplanation,
        String diseaseDescription,
        List<String> symptoms,
        List<String> causes,
        String treatment,
        String recommendedPesticides,
        String recommendedFertilizers,
        String organicTreatment,
        String preventionMeasures,
        String expectedRecoveryTime,
        List<DiseasePredictionOption> predictions,
        String imageUrl,
        String provider
) {
}
