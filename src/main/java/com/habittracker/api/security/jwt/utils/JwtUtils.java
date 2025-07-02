package com.habittracker.api.security.jwt.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class JwtUtils {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private JwtUtils() {}

  public static Optional<String> extractToken(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
        .filter(t -> t.startsWith(BEARER_PREFIX))
        .map(t -> t.substring(BEARER_PREFIX.length()));
  }
}
