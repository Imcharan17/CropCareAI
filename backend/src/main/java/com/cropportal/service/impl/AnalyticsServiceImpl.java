package com.cropportal.service.impl;

import com.cropportal.dto.AnalyticsResponse;
import com.cropportal.entity.TicketStatus;
import com.cropportal.repository.*;
import com.cropportal.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final DiseaseReportRepository diseaseReportRepository;

    @Override
    public AnalyticsResponse dashboard() {
        return new AnalyticsResponse(
                userRepository.count(),
                ticketRepository.countByStatus(TicketStatus.OPEN),
                ticketRepository.countByStatus(TicketStatus.RESOLVED),
                diseaseReportRepository.count(),
                Map.of("Late Blight", 42L, "Leaf Spot", 21L, "Powdery Mildew", 15L, "Healthy", 18L),
                Map.of("Open", ticketRepository.countByStatus(TicketStatus.OPEN), "Resolved", ticketRepository.countByStatus(TicketStatus.RESOLVED)),
                Map.of("Dr. Asha Verma", 34L, "Dr. Meera Rao", 22L, "Dr. Iqbal Khan", 18L)
        );
    }
}
