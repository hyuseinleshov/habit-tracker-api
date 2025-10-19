package com.habittracker.api.habit.service;

import com.habittracker.api.habit.dto.HabitStatisticResponse;
import java.util.UUID;

public interface StatisticsService {

  HabitStatisticResponse calculateStatistics(UUID habitId);
}
