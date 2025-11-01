package com.habittracker.api.security.jwt.service.impl;

import com.habittracker.api.security.jwt.service.JwtBlacklistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private static final String ACTIVE_JWT_KEY = "active:%s";
    private static final String BLACKLIST__KEY = "blacklist:%s";

    private final StringRedisTemplate redis;
    private final Duration jwtExpirationDuration;

    public JwtBlacklistServiceImpl(StringRedisTemplate redis, @Value("${jwt.expiration-duration}") Duration jwtExpirationDuration) {
        this.redis = redis;
        this.jwtExpirationDuration = jwtExpirationDuration;
    }

    @Override
    public void recordActiveToken(UUID userId, String token) {
        redis.opsForValue().set(String.format(ACTIVE_JWT_KEY, userId), token, jwtExpirationDuration);
    }


    @Override
    public void revokeActiveToken(UUID userId) {
        String key = String.format(ACTIVE_JWT_KEY, userId);
        String token = redis.opsForValue().getAndDelete(key);
        if(token != null) {
            addToBlacklist(token);
        }
    }

    private void addToBlacklist(String token) {
        redis.opsForValue().set(String.format(BLACKLIST__KEY, token), "1", jwtExpirationDuration);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return redis.opsForValue().get(String.format(BLACKLIST__KEY, token)) != null;
    }
}
