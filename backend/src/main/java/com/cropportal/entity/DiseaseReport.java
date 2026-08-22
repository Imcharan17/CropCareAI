package com.cropportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "disease_reports", indexes = {
        @Index(name = "idx_reports_farmer", columnList = "farmer_id"),
        @Index(name = "idx_reports_disease", columnList = "diseaseName")
})
public class DiseaseReport extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;
    @ManyToOne
    @JoinColumn(name = "crop_id")
    private Crop crop;
    @OneToOne(optional = false)
    @JoinColumn(name = "image_upload_id", nullable = false)
    private ImageUpload imageUpload;
    private String diseaseName;
    private Double confidenceScore;
    private String affectedArea;
    @Enumerated(EnumType.STRING)
    private SeverityLevel severityLevel;
    @Column(columnDefinition = "TEXT")
    private String confidenceExplanation;
    @Column(columnDefinition = "TEXT")
    private String diseaseDescription;
    @Column(columnDefinition = "TEXT")
    private String symptoms;
    @Column(columnDefinition = "TEXT")
    private String causes;
    @Column(columnDefinition = "TEXT")
    private String treatment;
    @Column(columnDefinition = "TEXT")
    private String recommendedPesticides;
    @Column(columnDefinition = "TEXT")
    private String recommendedFertilizers;
    @Column(columnDefinition = "TEXT")
    private String organicTreatment;
    @Column(columnDefinition = "TEXT")
    private String preventionMeasures;
    private String expectedRecoveryTime;
    @Column(columnDefinition = "TEXT")
    private String alternativePredictions;
    private String aiProvider;
}
