package com.cropportal.dto;

import com.cropportal.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String fullName, String email, String phone, boolean blocked,
                           Set<String> roles, Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.isBlocked(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()), user.getCreatedAt());
    }
}
