package com.cropportal.controller;

import com.cropportal.dto.DiseasePredictionResponse;
import com.cropportal.entity.DiseaseReport;
import com.cropportal.repository.DiseaseReportRepository;
import com.cropportal.service.DiseaseDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/disease")
@RequiredArgsConstructor
public class DiseaseController {
    private final DiseaseDetectionService diseaseDetectionService;
    private final DiseaseReportRepository diseaseReportRepository;

    @PostMapping("/detect")
    @PreAuthorize("hasRole('FARMER')")
    public DiseasePredictionResponse detect(Authentication authentication,
                                            @RequestParam(required = false) Long cropId,
                                            @RequestPart("image") MultipartFile image) {
        return diseaseDetectionService.detect(authentication.getName(), cropId, image);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('FARMER')")
    public Page<DiseaseReport> history(Authentication authentication, Pageable pageable) {
        return diseaseReportRepository.findByFarmerUserEmail(authentication.getName(), pageable);
    }
}
