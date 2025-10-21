package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.user.dto.UserStatisticsResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

  private final CheckInService checkInService;
  private final StreakService streakService;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userStatistics", key = "#user.id")
  public UserStatisticsResponse calculateStatistics(UserEntity user) {
    long totalCheckIns = checkInService.getUserCheckInsCount(user.getId());
    HabitStatisticResponse.BestStreakData userBestStreak = getUserBestStreak(user);
    long activeStreaks = streakService.getUserActiveStreaks(user.getId());
    LocalDate lastCheckInDate = checkInService.getUserLastCheckInDate(user.getId());
    return new UserStatisticsResponse(
        user.getId(), totalCheckIns, userBestStreak, activeStreaks, lastCheckInDate, Instant.now());
  }

  private static HabitStatisticResponse.BestStreakData getUserBestStreak(UserEntity user) {
    return user.getHabits().stream()
        .max(Comparator.comparing(HabitEntity::getBestStreak))
        .map(
            habit ->
                HabitStatisticResponse.BestStreakData.of(
                    habit.getBestStreak(), habit.getBestStreakStartDate(), habit.getId()))
        .orElse(HabitStatisticResponse.BestStreakData.of(0, null, null));
  }
}
