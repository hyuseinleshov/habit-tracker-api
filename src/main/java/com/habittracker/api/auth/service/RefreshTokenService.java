package com.habittracker.api.auth.service;

import java.util.UUID;

public interface RefreshTokenService {
  String createRefreshToken(UUID userId);

  boolean isValid(String refreshToken);

  UUID getUserIdFromRefreshToken(String refreshToken);

  void revokeRefreshToken(String refreshToken);

  void revokeAllRefreshTokensForUser(UUID userId);
}
