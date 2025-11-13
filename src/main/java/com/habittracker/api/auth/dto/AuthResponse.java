package com.habittracker.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthResponse(
    String email, String accessToken, @JsonIgnore String refreshToken, String message) {}
