package com.habittracker.api.habit.service.impl;

import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.service.StatisticsService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

  private final HabitHelper habitHelper;
  private final CheckInService checkInService;
  private final StreakService streakService;

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, principal.id)")
  @Cacheable(value = "habitStatistics", key = "#habitId")
  public HabitStatisticResponse calculateStatistics(UUID habitId) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    long totalCheckins = checkInService.getCheckInsCount(habitId);
    int currentStreak = streakService.calculateStreak(habitId).currentStreak();
    LocalDate lastCheckInDate = checkInService.getLastCheckInDate(habitId);
    return new HabitStatisticResponse(
        habitId,
        habit.getName(),
        totalCheckins,
        new HabitStatisticResponse.StreakData(currentStreak, buildBestSteakData(habit)),
        lastCheckInDate);
  }

  private HabitStatisticResponse.BestStreakData buildBestSteakData(HabitEntity habit) {
    LocalDate startDate = habit.getBestStreakStartDate();
    LocalDate endDate = startDate != null ? startDate.plusDays(habit.getBestStreak()) : null;
    return new HabitStatisticResponse.BestStreakData(habit.getBestStreak(), startDate, endDate);
  }
}
