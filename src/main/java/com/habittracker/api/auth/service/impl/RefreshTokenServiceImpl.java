package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.utils.AuthConstants.*;

import com.habittracker.api.auth.model.RefreshTokenEntity;
import com.habittracker.api.auth.repository.RefreshTokenRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public String createRefreshToken(String email) {
    String token = UUID.randomUUID().toString();
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setToken(token);
    entity.setEmail(email);
    entity.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS));
    refreshTokenRepository.save(entity);
    return token;
  }

  @Override
  public boolean isValid(String refreshToken) {
    Optional<RefreshTokenEntity> entityOpt = refreshTokenRepository.findByToken(refreshToken);
    return entityOpt.isPresent() && entityOpt.get().getExpiresAt().isAfter(Instant.now());
  }

  @Override
  public String getEmailFromRefreshToken(String refreshToken) {
    return refreshTokenRepository
        .findByToken(refreshToken)
        .map(RefreshTokenEntity::getEmail)
        .orElse(null);
  }

  @Override
  public void revokeRefreshToken(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }
}
