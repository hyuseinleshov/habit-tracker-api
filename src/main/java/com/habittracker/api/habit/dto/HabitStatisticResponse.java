package com.habittracker.api.habit.dto;

import com.habittracker.api.habit.streak.dto.StreakData;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HabitStatisticResponse(
    UUID id,
    String name,
    long totalCheckIns,
    StreakData streaks,
    LocalDate lastCheckin,
    Instant createdAt,
    Instant calculatedAt) {}
