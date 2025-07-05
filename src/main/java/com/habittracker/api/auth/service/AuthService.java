package com.habittracker.api.auth.service;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;

public interface AuthService {
  AuthResponse register(AuthRequest request);

  AuthResponse login(AuthRequest request);
}
