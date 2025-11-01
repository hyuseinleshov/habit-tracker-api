package com.habittracker.api.auth.service;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenService {
  String createRefreshToken(UUID userId);

  boolean isValid(String refreshToken);

  Optional<UUID> getUserIdFromRefreshToken(String refreshToken);

  void revokeRefreshToken(String refreshToken);

  void revokeAllRefreshTokensForUser(UUID userId);
}
