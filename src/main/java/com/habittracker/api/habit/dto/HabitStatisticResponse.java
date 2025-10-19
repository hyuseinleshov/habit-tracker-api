package com.habittracker.api.habit.dto;

import java.time.LocalDate;
import java.util.UUID;

public record HabitStatisticResponse(
    UUID id, String name, long totalCheckIns, StreakData streaks, LocalDate lastCheckin) {
  public record StreakData(long currentDays, BestStreakData best) {}

  public record BestStreakData(int days, LocalDate startDate, LocalDate endDate) {}
}
