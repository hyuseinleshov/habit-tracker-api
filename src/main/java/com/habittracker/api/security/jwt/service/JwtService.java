package com.habittracker.api.security.jwt.service;

import com.habittracker.api.auth.model.UserEntity;
import io.jsonwebtoken.Claims;
import java.util.Optional;

public interface JwtService {

  String generateToken(UserEntity user);

  boolean isValid(String token);

  Optional<Claims> getClaims(String token);
}
