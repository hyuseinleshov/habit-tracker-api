package com.habittracker.api.auth.service;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.dto.RefreshTokenRequest;
import com.habittracker.api.auth.dto.RefreshTokenResponse;

public interface AuthService {
  AuthResponse register(AuthRequest request);

  AuthResponse login(AuthRequest request);

  RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
