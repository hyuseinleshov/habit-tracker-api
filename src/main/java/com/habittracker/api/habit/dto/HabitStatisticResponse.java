package com.habittracker.api.habit.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HabitStatisticResponse(
    UUID id,
    String name,
    long totalCheckIns,
    StreakData streaks,
    LocalDate lastCheckin,
    Instant calculatedAt) {
  public record StreakData(long currentDays, BestStreakData best) {}

  public record BestStreakData(
      int days,
      LocalDate startDate,
      LocalDate endDate,
      UUID habitId) {
    public static BestStreakData of(int days, LocalDate startDate, UUID habitId) {
      LocalDate endDate = startDate != null ? startDate.plusDays(days - 1) : null;
      return new BestStreakData(days, startDate, endDate, habitId);
    }
  }
}
