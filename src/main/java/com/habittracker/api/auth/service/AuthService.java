package com.habittracker.api.auth.service;

import com.habittracker.api.auth.dto.*;

public interface AuthService {
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
