package com.cropportal.service;

import com.cropportal.dto.*;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(String email, ChangePasswordRequest request);
}
