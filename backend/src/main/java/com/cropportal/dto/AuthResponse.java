package com.cropportal.dto;

import java.util.Set;

public record AuthResponse(String token, String refreshToken, Long userId, String fullName, String email, Set<String> roles) {
}
