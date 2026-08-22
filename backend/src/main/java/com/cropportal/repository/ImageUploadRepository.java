package com.cropportal.repository;

import com.cropportal.entity.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageUploadRepository extends JpaRepository<ImageUpload, Long> {
}
