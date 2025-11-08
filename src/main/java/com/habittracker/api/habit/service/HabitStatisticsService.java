package com.habittracker.api.habit.service;

import com.habittracker.api.habit.dto.HabitStatisticResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface HabitStatisticsService {

  HabitStatisticResponse calculateStatistics(UUID habitId);

  HabitStatisticResponse.BestStreakData buildBestStreak(int streak, LocalDate streakStartDate, UUID habitId);
}
