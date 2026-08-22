package com.cropportal.ai;

import com.cropportal.dto.DiseasePredictionResponse;
import com.cropportal.dto.DiseasePredictionOption;
import com.cropportal.entity.SeverityLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "tensorflow")
public class TensorFlowProvider implements DiseaseDetectionProvider {
    @Override
    public DiseasePredictionResponse predict(MultipartFile image) {
        DiseasePredictionOption primary = new DiseasePredictionOption("TensorFlow model unavailable", 0.0,
                SeverityLevel.LOW, "Not evaluated");
        return new DiseasePredictionResponse(null, primary.diseaseName(), primary.confidenceScore(), primary.affectedArea(),
                primary.severityLevel(), "No confidence explanation is available because TensorFlow inference is not configured.",
                "TensorFlow inference is not configured in this deployment.",
                java.util.List.of(), java.util.List.of(),
                "Configure model artifacts before using tensorflow provider",
                "Configure model artifacts before using tensorflow provider",
                "Configure model artifacts before using tensorflow provider",
                "Configure model artifacts before using tensorflow provider",
                "Set APP_AI_PROVIDER=mock until the trained model is mounted",
                "Unknown", java.util.List.of(primary), null, name());
    }

    @Override
    public String name() {
        return "tensorflow";
    }
}
