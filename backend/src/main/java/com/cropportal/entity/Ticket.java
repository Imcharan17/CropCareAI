package com.cropportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_tickets_status", columnList = "status"),
        @Index(name = "idx_tickets_farmer", columnList = "farmer_id"),
        @Index(name = "idx_tickets_doctor", columnList = "doctor_id")
})
public class Ticket extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    @ManyToOne
    @JoinColumn(name = "disease_report_id")
    private DiseaseReport diseaseReport;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;
    private String priority = "MEDIUM";
    @Column(columnDefinition = "TEXT")
    private String treatmentRecommendation;
    private String prescriptionDocumentPath;
}
