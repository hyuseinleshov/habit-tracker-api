package com.habittracker.api.auth.service;

import com.habittracker.api.auth.dto.*;
import java.util.UUID;

public interface AuthService {
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  RefreshTokenResponse refreshToken(String refreshToken);

  void logout(UUID userId);
}
