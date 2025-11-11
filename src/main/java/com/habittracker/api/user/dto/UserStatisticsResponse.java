package com.habittracker.api.user.dto;

import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.streak.dto.BestStreakData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserStatisticsResponse(
    UUID id,
    long totalCheckIns,
    BestStreakData bestStreak,
    long activeStreaks,
    LocalDate lastCheckInDate,
    Instant calculatedAt) {}
