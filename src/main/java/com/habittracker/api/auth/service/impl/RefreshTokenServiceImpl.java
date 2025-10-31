package com.habittracker.api.auth.service.impl;

import com.habittracker.api.auth.model.RefreshTokenEntity;
import com.habittracker.api.auth.repository.RefreshTokenRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${refresh-token.expiration-duration}")
  private Duration refreshTokenExpiration;

  @Override
  @Transactional
  public String createRefreshToken(UUID userId) {
    String token = UUID.randomUUID().toString();
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setToken(token);
    entity.setUserId(userId);
    entity.setExpiresAt(Instant.now().plus(refreshTokenExpiration));
    refreshTokenRepository.save(entity);
    return token;
  }

  @Override
  public boolean isValid(String refreshToken) {
    Optional<RefreshTokenEntity> entityOpt = refreshTokenRepository.findByToken(refreshToken);
    return entityOpt.isPresent() && entityOpt.get().getExpiresAt().isAfter(Instant.now());
  }

  @Override
  public UUID getUserIdFromRefreshToken(String refreshToken) {
    return refreshTokenRepository
        .findByToken(refreshToken)
        .map(RefreshTokenEntity::getId)
        .orElse(null);
  }

  @Override
  public void revokeRefreshToken(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }

  @Override
  public void revokeAllRefreshTokensForUser(UUID userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }
}
