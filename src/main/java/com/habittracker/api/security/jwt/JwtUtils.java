package com.habittracker.api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class JwtUtils {

    public static Optional<String> extractToken(HttpServletRequest request) {
        return Optional.empty();
    }
}
