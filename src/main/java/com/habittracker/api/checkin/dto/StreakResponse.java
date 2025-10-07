package com.habittracker.api.checkin.dto;

import java.time.Instant;
import java.util.UUID;

public record StreakResponse(UUID habitId, int currentStreak, Instant calculatedAt) {}
