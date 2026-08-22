package com.cropportal.dto;

import java.util.Map;

public record AnalyticsResponse(long activeUsers, long openTickets, long resolvedTickets, long totalReports,
                                Map<String, Long> diseaseDistribution,
                                Map<String, Long> ticketResolutionRate,
                                Map<String, Long> doctorPerformance) {
}
