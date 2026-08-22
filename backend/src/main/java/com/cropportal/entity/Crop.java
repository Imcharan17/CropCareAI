package com.cropportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crops", indexes = @Index(name = "idx_crops_name", columnList = "name"))
public class Crop extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String season;
    private String description;
}
