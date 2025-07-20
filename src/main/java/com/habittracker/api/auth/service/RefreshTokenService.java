package com.habittracker.api.auth.service;

public interface RefreshTokenService {
  String createRefreshToken(String email);

  boolean isValid(String refreshToken);

  String getEmailFromRefreshToken(String refreshToken);

  void revokeRefreshToken(String refreshToken);

  void revokeAllRefreshTokensForUser(String email);
}
