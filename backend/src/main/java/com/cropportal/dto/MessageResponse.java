package com.cropportal.dto;

import java.time.Instant;

public record MessageResponse(Long id, Long ticketId, String senderName, String message, boolean readReceipt, Instant createdAt) {
}
