package com.cropportal.dto;

import java.util.Map;

public record ReportDashboardResponse(
        long totalDiseaseReports,
        long totalFarmers,
        long activeFarmers,
        long totalAiScans,
        long healthyCropReports,
        long diseasedCropReports,
        long pendingTickets,
        long resolvedTickets,
        long blockedUsers,
        double averageAiConfidence,
        String mostAffectedCrop,
        String mostCommonDisease,
        String mostActiveFarmer,
        long reportsGeneratedToday,
        long reportsGeneratedThisMonth,
        long detectionsThisWeek,
        Map<String, Long> diseaseDistribution,
        Map<String, Long> cropWiseReports,
        Map<String, Long> monthlyDetectionTrend,
        Map<String, Long> severityDistribution,
        Map<String, Long> districtWiseDiseaseDistribution
) {
}
