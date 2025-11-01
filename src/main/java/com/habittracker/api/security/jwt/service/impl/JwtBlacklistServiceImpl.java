package com.habittracker.api.security.jwt.service.impl;

import com.habittracker.api.security.jwt.service.JwtBlacklistService;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

  private static final String ACTIVE_JWT_KEY = "active:%s";
  private static final String BLACKLIST__KEY = "blacklist:%s";

  private final StringRedisTemplate redis;
  private final Duration jwtExpirationDuration;

  public JwtBlacklistServiceImpl(
      StringRedisTemplate redis,
      @Value("${jwt.expiration-duration}") Duration jwtExpirationDuration) {
    this.redis = redis;
    this.jwtExpirationDuration = jwtExpirationDuration;
  }

  @Override
  public void recordActiveToken(UUID userId, String jti) {
    redis.opsForValue().set(String.format(ACTIVE_JWT_KEY, userId), jti, jwtExpirationDuration);
  }

  @Override
  public void revokeActiveToken(UUID userId) {
    String key = String.format(ACTIVE_JWT_KEY, userId);
    String jid = redis.opsForValue().getAndDelete(key);
    if (jid != null) {
      addToBlacklist(jid);
    }
  }

  private void addToBlacklist(String jid) {
    redis.opsForValue().set(String.format(BLACKLIST__KEY, jid), "1", jwtExpirationDuration);
  }

  @Override
  public boolean isBlacklisted(String jti) {
    return redis.opsForValue().get(String.format(BLACKLIST__KEY, jti)) != null;
  }
}
