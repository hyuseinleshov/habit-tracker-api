package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.utils.AuthUtils.getUserTimeZone;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.user.dto.UserStatisticsResponse;
import com.habittracker.api.user.service.UserStatisticsService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

  private final CheckInService checkInService;
  private final HabitRepository habitRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userStatistics", key = "#user.id")
  public UserStatisticsResponse calculateStatistics(UserEntity user) {
    long totalCheckIns = checkInService.getUserCheckInsCount(user.getId());
    HabitStatisticResponse.BestStreakData userBestStreak = getUserBestStreak(user.getId());
    long activeStreaks = getUserActiveStreaks(user.getId());
    LocalDate lastCheckInDate = checkInService.getUserLastCheckInDate(user.getId());
    return new UserStatisticsResponse(
        user.getId(), totalCheckIns, userBestStreak, activeStreaks, lastCheckInDate, Instant.now());
  }

  private long getUserActiveStreaks(UUID userId) {
    ZoneId userTimeZone = getUserTimeZone();
    LocalDate yesterday = LocalDate.now(userTimeZone).minusDays(1);
    Instant since = yesterday.atStartOfDay(userTimeZone).toInstant();
    return habitRepository.countHabitsWithRecentCheckIns(userId, since);
  }

  private HabitStatisticResponse.BestStreakData getUserBestStreak(UUID userId) {
    return habitRepository
        .findBestStreakByUserId(userId)
        .map(
            habit ->
                HabitStatisticResponse.BestStreakData.of(
                    habit.getBestStreak(), habit.getBestStreakStartDate(), habit.getId()))
        .orElse(HabitStatisticResponse.BestStreakData.of(0, null, null));
  }
}
