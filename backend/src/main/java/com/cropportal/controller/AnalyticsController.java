package com.cropportal.controller;

import com.cropportal.dto.AnalyticsResponse;
import com.cropportal.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public AnalyticsResponse dashboard() {
        return analyticsService.dashboard();
    }
}
