package com.habittracker.api.auth.dto;

public record RefreshTokenResponse(String accessToken, String refreshToken, String message) {}
