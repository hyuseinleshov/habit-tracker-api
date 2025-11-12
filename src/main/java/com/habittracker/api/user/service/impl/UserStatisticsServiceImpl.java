package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.utils.AuthUtils.getUserTimeZone;

import com.habittracker.api.auth.utils.AuthUtils;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.streak.dto.BestStreakData;
import com.habittracker.api.habit.streak.service.StreakService;
import com.habittracker.api.user.dto.DailyCheckinSummary;
import com.habittracker.api.user.dto.UserStatisticsResponse;
import com.habittracker.api.user.dto.WeeklySummaryResponse;
import com.habittracker.api.user.service.UserStatisticsService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

  private final CheckInService checkInService;
  private final HabitRepository habitRepository;
  private final StreakService streakService;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userStatistics", key = "#id")
  public UserStatisticsResponse calculateStatistics(UUID id) {
    long totalCheckIns = checkInService.getUserCheckInsCount(id);
    BestStreakData userBestStreak = getBestStreak(id);
    long activeStreaks = getUserActiveStreaks(id);
    LocalDate lastCheckInDate = checkInService.getUserLastCheckInDate(id);
    return new UserStatisticsResponse(
        id, totalCheckIns, userBestStreak, activeStreaks, lastCheckInDate, Instant.now());
  }

  @Override
  @Cacheable(value = "weeklySummary", key = "#id")
  public WeeklySummaryResponse getWeeklySummary(UUID id) {
    long habitCount = habitRepository.countByUserId(id);
    long todayCheckins = checkInService.getCheckInsToday(id);
    List<DailyCheckinSummary> weeklyStats = getWeeklyStats(id);
    return new WeeklySummaryResponse(habitCount, todayCheckins, weeklyStats);
  }

  @Override
  public BestStreakData getBestStreak(UUID userId) {
    return habitRepository
        .findBestStreakByUserId(userId)
        .map(
            habit ->
                streakService.buildBestStreak(
                    habit.getBestStreak(), habit.getBestStreakStartDate(), habit.getId()))
        .orElse(new BestStreakData(0, null, null, null));
  }

  private List<DailyCheckinSummary> getWeeklyStats(UUID userId) {
    ZoneId userTimeZone = AuthUtils.getUserTimeZone();
    LocalDate startDate = LocalDate.now(userTimeZone).minusDays(7);
    LocalDate endDate = LocalDate.now(userTimeZone).minusDays(1);

    Set<CheckInEntity> checkIns = checkInService.getCheckInsFor(userId, startDate, endDate);
    return checkIns.stream()
        .collect(Collectors.groupingBy(c -> c.getCreatedAt().atZone(userTimeZone).toLocalDate()))
        .entrySet()
        .stream()
        .map(e -> new DailyCheckinSummary(e.getKey(), e.getValue().size()))
        .sorted(Comparator.comparing(DailyCheckinSummary::date))
        .toList();
  }

  private long getUserActiveStreaks(UUID userId) {
    ZoneId userTimeZone = getUserTimeZone();
    LocalDate yesterday = LocalDate.now(userTimeZone).minusDays(1);
    Instant since = yesterday.atStartOfDay(userTimeZone).toInstant();
    return habitRepository.countHabitsWithRecentCheckIns(userId, since);
  }
}
