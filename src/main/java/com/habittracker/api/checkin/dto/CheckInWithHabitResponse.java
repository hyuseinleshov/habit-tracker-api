package com.habittracker.api.checkin.dto;

import java.time.Instant;
import java.util.UUID;

public record CheckInWithHabitResponse(UUID id, UUID habitId, Instant createdAt) {}
