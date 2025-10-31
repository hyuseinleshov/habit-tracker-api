package com.habittracker.api.auth.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieUtils {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_HEADER = "Set-Cookie";
    private final Duration refreshTokenDuration;

    public RefreshTokenCookieUtils(@Value("${refresh-token.expiration-duration}") Duration refreshTokenDuration) {
        this.refreshTokenDuration = refreshTokenDuration;
    }

    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response ) {
        response.setHeader(COOKIE_HEADER, buildRefreshTokenCookie(refreshToken, refreshTokenDuration).toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {

        response.setHeader(COOKIE_HEADER, buildRefreshTokenCookie("", Duration.ZERO).toString());
    }

    private static ResponseCookie buildRefreshTokenCookie(String refreshToken, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }
}
