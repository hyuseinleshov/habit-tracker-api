package com.habittracker.api.security.jwt;

public interface JwtService {

    String generateToken(String email);
}
