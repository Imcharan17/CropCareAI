package com.cropportal.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketRequest(@NotBlank String title, String description, Long diseaseReportId, String priority) {
}
