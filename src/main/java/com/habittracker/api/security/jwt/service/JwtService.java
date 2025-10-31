package com.habittracker.api.security.jwt.service;

import com.habittracker.api.auth.model.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface JwtService {

  String generateToken(UserEntity user);

  boolean isValid(String token);

  Optional<String> extractSubject(String token);
}
