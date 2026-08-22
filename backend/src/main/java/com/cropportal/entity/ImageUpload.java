package com.cropportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "image_uploads", indexes = @Index(name = "idx_image_farmer", columnList = "farmer_id"))
public class ImageUpload extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long sizeBytes;
    private String storageProvider = "LOCAL";
    private String storagePath;
}
