package com.habittracker.api.user.dto;

import java.time.Instant;

public record AdminUserDTO(
    String email,
    String timezone,
    String firstName,
    String lastName,
    Integer age,
    Instant deletedAt) {}
