package com.habittracker.api.habit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.UUID;

public record HabitStatisticResponse(
    UUID id, String name, long totalCheckIns, StreakData streaks, LocalDate lastCheckin) {
  public record StreakData(long currentDays, BestStreakData best) {}

  public record BestStreakData(
      int days,
      LocalDate startDate,
      LocalDate endDate,
      @JsonInclude(JsonInclude.Include.NON_NULL) UUID habitId) {
    public static BestStreakData of(int days, LocalDate startDate, LocalDate endDate) {
      return new BestStreakData(days, startDate, endDate, null);
    }

    public static BestStreakData of(
        int days, LocalDate startDate, LocalDate endDate, UUID habitId) {
      return new BestStreakData(days, startDate, endDate, habitId);
    }
  }
}
