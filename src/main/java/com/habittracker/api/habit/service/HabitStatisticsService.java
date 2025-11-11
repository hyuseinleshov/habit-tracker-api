package com.habittracker.api.habit.service;

import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.streak.dto.BestStreakData;

import java.time.LocalDate;
import java.util.UUID;

public interface HabitStatisticsService {

  HabitStatisticResponse calculateStatistics(UUID habitId);

}
