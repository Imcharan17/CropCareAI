package com.cropportal.service.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropportal.dto.AuthRequest;
import com.cropportal.dto.AuthResponse;
import com.cropportal.dto.ChangePasswordRequest;
import com.cropportal.dto.ForgotPasswordRequest;
import com.cropportal.dto.RefreshTokenRequest;
import com.cropportal.dto.RegisterRequest;
import com.cropportal.dto.ResetPasswordRequest;
import com.cropportal.entity.Doctor;
import com.cropportal.entity.Farmer;
import com.cropportal.entity.Role;
import com.cropportal.entity.RoleName;
import com.cropportal.entity.User;
import com.cropportal.exception.BadRequestException;
import com.cropportal.notification.MailService;
import com.cropportal.repository.DoctorRepository;
import com.cropportal.repository.FarmerRepository;
import com.cropportal.repository.RoleRepository;
import com.cropportal.repository.UserRepository;
import com.cropportal.security.JwtService;
import com.cropportal.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FarmerRepository farmerRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

    @Override
    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        if (user.isBlocked()) {
            throw new BadRequestException("User account is blocked");
        }

        return response(user);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() ->
                        new BadRequestException("Role not found"));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());

        // IMPORTANT: Use mutable HashSet instead of Set.of()
        user.setRoles(new HashSet<>(Set.of(role)));

        user = userRepository.save(user);

        if (request.role() == RoleName.ROLE_FARMER) {

            Farmer farmer = new Farmer();
            farmer.setUser(user);
            farmer.setFarmLocation(request.farmLocation());
            farmer.setPrimaryCrop(request.primaryCrop());

            farmerRepository.save(farmer);
        }

        if (request.role() == RoleName.ROLE_DOCTOR) {

            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialization(request.specialization());

            doctorRepository.save(doctor);
        }

        return response(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() ->
                        new BadRequestException("Invalid refresh token"));

        return response(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.email()).ifPresent(user -> {

            String token = UUID.randomUUID().toString();

            user.setPasswordResetToken(token);
            user.setPasswordResetExpiresAt(
                    Instant.now().plusSeconds(900)
            );

            mailService.send(
                    user.getEmail(),
                    "CropCare Password Reset",
                    "Use this reset token: " + token
            );
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() ->
                        new BadRequestException("Invalid reset token"));

        if (user.getPasswordResetExpiresAt() == null ||
                user.getPasswordResetExpiresAt().isBefore(Instant.now())) {

            throw new BadRequestException("Reset token expired");
        }

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
    }

    @Override
    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "Current password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );
    }

    private AuthResponse response(User user) {

        String refreshToken = UUID.randomUUID().toString();

        user.setRefreshToken(refreshToken);

        // No save() needed here.
        // The transaction will automatically persist the updated refreshToken.

        return new AuthResponse(
                jwtService.generate(user),
                refreshToken,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}