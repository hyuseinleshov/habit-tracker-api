package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.utils.AuthUtils.getUserTimeZone;

import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.service.HabitStatisticsService;
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
  private final HabitStatisticsService habitStatisticsService;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userStatistics", key = "#id")
  public UserStatisticsResponse calculateStatistics(UUID id) {
    long totalCheckIns = checkInService.getUserCheckInsCount(id);
    HabitStatisticResponse.BestStreakData userBestStreak = getUserBestStreak(id);
    long activeStreaks = getUserActiveStreaks(id);
    LocalDate lastCheckInDate = checkInService.getUserLastCheckInDate(id);
    return new UserStatisticsResponse(
        id, totalCheckIns, userBestStreak, activeStreaks, lastCheckInDate, Instant.now());
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
                habitStatisticsService.buildBestStreak(
                    habit.getBestStreak(), habit.getBestStreakStartDate(), habit.getId()))
        .orElse(new HabitStatisticResponse.BestStreakData(0, null, null, null));
  }
}
