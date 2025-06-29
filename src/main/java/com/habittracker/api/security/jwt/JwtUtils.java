package com.habittracker.api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public final class JwtUtils {

    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String BEARER_PREFIX = "Bearer ";

    private JwtUtils() {

    }


    public static Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(t -> t.startsWith(BEARER_PREFIX))
                .map(t -> t.substring(BEARER_PREFIX.length()));
    }
}