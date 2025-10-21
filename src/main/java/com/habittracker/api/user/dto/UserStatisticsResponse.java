package com.habittracker.api.user.dto;

import com.habittracker.api.habit.dto.HabitStatisticResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserStatisticsResponse(
    UUID id,
    long totalCheckIns,
    HabitStatisticResponse.BestStreakData bestStreak,
    List<ActiveStreak> activeStreaks,
    LocalDate lastCheckInDate,
    Instant calculatedAt) {

  public record ActiveStreak(int streak, UUID habitId) {}
}
