package com.habittracker.api.user.service;

import com.habittracker.api.habit.streak.dto.BestStreakData;
import com.habittracker.api.user.dto.UserStatisticsResponse;
import com.habittracker.api.user.dto.WeeklySummaryResponse;
import java.util.UUID;

public interface UserStatisticsService {

  UserStatisticsResponse calculateStatistics(UUID id);

  WeeklySummaryResponse getWeeklySummary(UUID id);

  BestStreakData getBestStreak(UUID userId);
}
