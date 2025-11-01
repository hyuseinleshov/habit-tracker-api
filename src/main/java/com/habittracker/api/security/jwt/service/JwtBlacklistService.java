package com.habittracker.api.security.jwt.service;

import java.util.UUID;

public interface JwtBlacklistService {

  void recordActiveToken(UUID userId, String jti);

  void revokeActiveToken(UUID userId);

  boolean isBlacklisted(String jti);
}
