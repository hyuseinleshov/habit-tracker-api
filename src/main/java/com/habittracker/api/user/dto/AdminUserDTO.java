package com.habittracker.api.user.dto;

import java.time.Instant;

public record AdminUserDTO(
    String email,
    String timeZone,
    String firstName,
    String lastName,
    Integer age,
    Instant deletedAt) {}
