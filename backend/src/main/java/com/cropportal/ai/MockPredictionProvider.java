package com.cropportal.ai;

import com.cropportal.dto.DiseasePredictionResponse;
import com.cropportal.dto.DiseasePredictionOption;
import com.cropportal.entity.SeverityLevel;
import com.cropportal.exception.AiPredictionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockPredictionProvider implements DiseaseDetectionProvider {
    private static final List<MockDisease> DISEASES = List.of(
            new MockDisease(
                    "Tomato Late Blight",
                    SeverityLevel.HIGH,
                    "Leaf canopy and stem edges",
                    "A fungal-like blight pattern commonly associated with humid crop conditions.",
                    List.of("Dark water-soaked lesions", "Yellowing around leaf spots", "Stem edge discoloration"),
                    List.of("High humidity", "Poor airflow", "Infected plant residue"),
                    "Remove infected leaves and apply locally approved fungicide under expert guidance.",
                    "Copper oxychloride spray; Mancozeb as locally approved",
                    "Balanced NPK, calcium nitrate, composted organic matter",
                    "Neem extract or bio-fungicide where approved; remove infected debris safely",
                    "Remove infected leaves, improve airflow, avoid overhead irrigation, rotate crops",
                    "10-14 days with timely treatment"
            ),
            new MockDisease(
                    "Bacterial Leaf Spot",
                    SeverityLevel.MODERATE,
                    "Scattered leaf lesions",
                    "A bacterial spotting pattern that often starts as small dark lesions on leaves.",
                    List.of("Small brown-black spots", "Yellow halos", "Leaf curling in advanced areas"),
                    List.of("Splashing water", "Contaminated tools", "Warm wet weather"),
                    "Prune affected foliage and use copper-based treatment where locally approved.",
                    "Copper hydroxide or copper oxychloride as locally approved",
                    "Potassium-rich balanced fertilizer and compost to support recovery",
                    "Use neem oil or approved bio-control products and sanitize tools",
                    "Avoid overhead watering, space plants well, and remove crop debris",
                    "7-12 days after conditions improve"
            ),
            new MockDisease(
                    "Powdery Mildew",
                    SeverityLevel.MODERATE,
                    "Upper leaf surface",
                    "A white powdery fungal growth that weakens leaves and slows photosynthesis.",
                    List.of("White powder-like patches", "Leaf yellowing", "Dry curled leaf edges"),
                    List.of("Dense canopy", "Dry days with humid nights", "Poor air circulation"),
                    "Improve airflow and apply sulfur or potassium bicarbonate if recommended locally.",
                    "Wettable sulfur or potassium bicarbonate as locally approved",
                    "Avoid excess nitrogen; use balanced NPK with micronutrients",
                    "Spray diluted neem oil in cooler hours if appropriate for the crop",
                    "Increase plant spacing, remove infected leaves, and monitor humidity",
                    "5-10 days with early control"
            ),
            new MockDisease(
                    "Healthy Crop",
                    SeverityLevel.LOW,
                    "No clear affected area",
                    "The uploaded image does not show strong visual symptoms of a major crop disease.",
                    List.of("No dominant lesion pattern detected", "Leaf color appears mostly normal"),
                    List.of("No active disease cause identified from the image"),
                    "Continue monitoring and keep routine crop care practices.",
                    "No pesticide recommended unless symptoms appear",
                    "Maintain soil-test-based fertilizer schedule",
                    "Compost, mulching, and routine field sanitation",
                    "Inspect leaves weekly and avoid unnecessary chemical application",
                    "No recovery period required"
            ),
            new MockDisease(
                    "Leaf Rust",
                    SeverityLevel.HIGH,
                    "Lower and middle leaves",
                    "Rust-like pustules indicate a fungal infection that can spread quickly in favorable weather.",
                    List.of("Orange-brown pustules", "Premature yellowing", "Reduced leaf vigor"),
                    List.of("Windborne spores", "Moist leaf surfaces", "Susceptible crop variety"),
                    "Remove infected leaves and apply a recommended fungicide after expert confirmation.",
                    "Triazole or strobilurin fungicide where locally approved",
                    "Balanced potassium and micronutrients to support leaf health",
                    "Use resistant varieties and approved bio-fungicides where available",
                    "Rotate crops, remove volunteer plants, and monitor after rainfall",
                    "10-21 days depending on severity"
            )
    );

    @Override
    public DiseasePredictionResponse predict(MultipartFile image) {
        int index = imageBucket(image);
        MockDisease disease = DISEASES.get(index);
        DiseasePredictionOption primary = new DiseasePredictionOption(disease.name(), confidence(index), disease.severity(),
                disease.affectedArea());
        DiseasePredictionOption secondary = secondaryPrediction(index);
        return new DiseasePredictionResponse(null, primary.diseaseName(), primary.confidenceScore(), primary.affectedArea(),
                primary.severityLevel(), explanation(primary, disease), disease.description(), disease.symptoms(), disease.causes(), disease.treatment(),
                disease.pesticides(), disease.fertilizers(), disease.organicTreatment(), disease.prevention(),
                disease.recoveryTime(), List.of(primary, secondary),
                null, name());
    }

    @Override
    public String name() {
        return "mock";
    }

    private int imageBucket(MultipartFile image) {
        try {
            CRC32 crc32 = new CRC32();
            crc32.update(image.getBytes());
            String filename = image.getOriginalFilename() == null ? "" : image.getOriginalFilename();
            crc32.update(filename.getBytes());
            return Math.floorMod((int) crc32.getValue(), DISEASES.size());
        } catch (IOException ex) {
            throw new AiPredictionException("Could not analyze uploaded image", ex);
        }
    }

    private Double confidence(int index) {
        return switch (index) {
            case 0 -> 0.93;
            case 1 -> 0.86;
            case 2 -> 0.89;
            case 3 -> 0.78;
            default -> 0.82;
        };
    }

    private DiseasePredictionOption secondaryPrediction(int primaryIndex) {
        int index = (primaryIndex + 1) % DISEASES.size();
        MockDisease disease = DISEASES.get(index);
        return new DiseasePredictionOption(disease.name(), Math.max(0.31, confidence(index) - 0.34),
                disease.severity(), disease.affectedArea());
    }

    private String explanation(DiseasePredictionOption primary, MockDisease disease) {
        return "The model matched " + String.join(", ", disease.symptoms())
                + " with " + primary.diseaseName() + " and estimated "
                + Math.round(primary.confidenceScore() * 100) + "% confidence from the uploaded crop image.";
    }

    private record MockDisease(
            String name,
            SeverityLevel severity,
            String affectedArea,
            String description,
            List<String> symptoms,
            List<String> causes,
            String treatment,
            String pesticides,
            String fertilizers,
            String organicTreatment,
            String prevention,
            String recoveryTime
    ) {
    }
}
