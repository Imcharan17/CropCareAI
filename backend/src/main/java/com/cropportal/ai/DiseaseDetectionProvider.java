package com.cropportal.ai;

import com.cropportal.dto.DiseasePredictionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DiseaseDetectionProvider {
    DiseasePredictionResponse predict(MultipartFile image);
    String name();
}
