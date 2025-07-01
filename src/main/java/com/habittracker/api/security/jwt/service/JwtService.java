package com.habittracker.api.security.jwt.service;

public interface JwtService {

    String generateToken(String email);

    boolean isValid(String token);
}
