package com.habittracker.api.security.jwt.service;

import java.util.Optional;
import java.util.UUID;

public interface JwtService {

  String generateToken(UUID userId);

  boolean isValid(String token);

  Optional<String> extractSubject(String token);
}
