package com.cropportal.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cropportal.ai.DiseaseDetectionProvider;
import com.cropportal.dto.DiseasePredictionOption;
import com.cropportal.dto.DiseasePredictionResponse;
import com.cropportal.entity.Crop;
import com.cropportal.entity.DiseaseReport;
import com.cropportal.entity.Farmer;
import com.cropportal.entity.ImageUpload;
import com.cropportal.exception.BadRequestException;
import com.cropportal.exception.ResourceNotFoundException;
import com.cropportal.repository.CropRepository;
import com.cropportal.repository.DiseaseReportRepository;
import com.cropportal.repository.FarmerRepository;
import com.cropportal.repository.ImageUploadRepository;
import com.cropportal.service.DiseaseDetectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseDetectionServiceImpl implements DiseaseDetectionService {
    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private static final TypeReference<List<DiseasePredictionOption>> PREDICTION_LIST = new TypeReference<>() {};

    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final ImageUploadRepository imageUploadRepository;
    private final DiseaseReportRepository diseaseReportRepository;
    private final DiseaseDetectionProvider detectionProvider;
    private final ObjectMapper objectMapper;
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public DiseasePredictionResponse detect(String farmerEmail, Long cropId, MultipartFile file) {
        validateImage(file);
        Farmer farmer = farmerRepository.findByUserEmail(farmerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found"));
        Crop crop = cropId == null ? null : cropRepository.findById(cropId).orElse(null);
        ImageUpload image = store(farmer, file);
        log.info("Running {} crop disease detection for farmer {}", detectionProvider.name(), farmerEmail);
        DiseaseReport report = runPrediction(farmer, crop, image, detectionProvider.predict(file));
        diseaseReportRepository.save(report);
        return toResponse(report);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Image is required");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new BadRequestException("Image must be 10MB or smaller");
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!type.equals("image/jpeg") && !type.equals("image/jpg") && !type.equals("image/png")) {
            throw new BadRequestException("Only JPG, JPEG and PNG images are supported");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg") && !filename.endsWith(".png")) {
            throw new BadRequestException("Image file extension must be JPG, JPEG or PNG");
        }
    }

    private ImageUpload store(Farmer farmer, MultipartFile file) {
        try {
            Files.createDirectories(Path.of(uploadDir));
            String filename = file.getOriginalFilename() == null ? "crop.jpg" : Path.of(file.getOriginalFilename()).getFileName().toString();
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT) : ".jpg";
            String stored = UUID.randomUUID() + ext;
            Path target = Path.of(uploadDir, stored);
            Files.copy(file.getInputStream(), target);
            ImageUpload image = new ImageUpload();
            image.setFarmer(farmer);
            image.setOriginalFileName(file.getOriginalFilename());
            image.setStoredFileName(stored);
            image.setContentType(file.getContentType());
            image.setSizeBytes(file.getSize());
            image.setStoragePath(target.toString());
            return imageUploadRepository.save(image);
        } catch (IOException ex) {
            throw new BadRequestException("Could not store image");
        }
    }

    private DiseaseReport runPrediction(Farmer farmer, Crop crop, ImageUpload image, DiseasePredictionResponse prediction) {
        DiseaseReport report = new DiseaseReport();
        report.setFarmer(farmer);
        report.setCrop(crop);
        report.setImageUpload(image);
        report.setDiseaseName(prediction.diseaseName());
        report.setConfidenceScore(prediction.confidenceScore());
        report.setAffectedArea(prediction.affectedArea());
        report.setSeverityLevel(prediction.severityLevel());
        report.setConfidenceExplanation(prediction.confidenceExplanation());
        report.setDiseaseDescription(prediction.diseaseDescription());
        report.setSymptoms(toJson(prediction.symptoms()));
        report.setCauses(toJson(prediction.causes()));
        report.setTreatment(prediction.treatment());
        report.setRecommendedPesticides(prediction.recommendedPesticides());
        report.setRecommendedFertilizers(prediction.recommendedFertilizers());
        report.setOrganicTreatment(prediction.organicTreatment());
        report.setPreventionMeasures(prediction.preventionMeasures());
        report.setExpectedRecoveryTime(prediction.expectedRecoveryTime());
        report.setAlternativePredictions(toJson(prediction.predictions()));
        report.setAiProvider(prediction.provider() == null ? detectionProvider.name() : prediction.provider());
        return report;
    }

    private DiseasePredictionResponse toResponse(DiseaseReport report) {
        return new DiseasePredictionResponse(report.getId(), report.getDiseaseName(), report.getConfidenceScore(),
                report.getAffectedArea(), report.getSeverityLevel(), report.getConfidenceExplanation(), report.getDiseaseDescription(),
                fromJson(report.getSymptoms(), STRING_LIST), fromJson(report.getCauses(), STRING_LIST),
                report.getTreatment(), report.getRecommendedPesticides(), report.getRecommendedFertilizers(),
                report.getOrganicTreatment(), report.getPreventionMeasures(), report.getExpectedRecoveryTime(),
                fromJson(report.getAlternativePredictions(), PREDICTION_LIST),
                "/api/files/" + report.getImageUpload().getStoredFileName(), report.getAiProvider());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Could not serialize prediction details");
        }
    }

    private <T> T fromJson(String value, TypeReference<T> type) {
        try {
            return objectMapper.readValue(value == null || value.isBlank() ? "[]" : value, type);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Could not read prediction details");
        }
    }
}
