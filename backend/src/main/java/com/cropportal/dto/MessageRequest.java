package com.cropportal.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(@NotBlank String message) {
}
