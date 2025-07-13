package com.habittracker.api.auth.dto;

public record AuthResponse(String email, String token, String message) {}
