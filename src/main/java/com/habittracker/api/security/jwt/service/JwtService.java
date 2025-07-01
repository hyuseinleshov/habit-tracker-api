package com.habittracker.api.security.jwt.service;

import java.util.Optional;

public interface JwtService {

    String generateToken(String email);

    boolean isValid(String token);

    Optional<String> extractSubject(String token);
}
