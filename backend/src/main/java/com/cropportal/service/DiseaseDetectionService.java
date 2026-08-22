package com.cropportal.service;

import com.cropportal.dto.DiseasePredictionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DiseaseDetectionService {
    DiseasePredictionResponse detect(String farmerEmail, Long cropId, MultipartFile file);
}
