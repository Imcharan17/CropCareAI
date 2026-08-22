package com.cropportal.dto;

import com.cropportal.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Size(min = 8) String password,
        String phone,
        @NotNull RoleName role,
        String farmLocation,
        String primaryCrop,
        String specialization
) {
}
