package com.cropportal.controller;

import com.cropportal.audit.AuditService;
import com.cropportal.dto.UserResponse;
import com.cropportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final AuditService auditService;

    @GetMapping
    public Page<UserResponse> users(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @PatchMapping("/{id}/block")
    public UserResponse block(Authentication authentication, @PathVariable Long id, @RequestParam boolean blocked) {
        com.cropportal.entity.User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(blocked);
        com.cropportal.entity.User actor = userRepository.findByEmail(authentication.getName()).orElse(null);
        auditService.record(actor, blocked ? "USER_BLOCKED" : "USER_UNBLOCKED", "USER", id, user.getEmail());
        return UserResponse.from(userRepository.save(user));
    }
}
