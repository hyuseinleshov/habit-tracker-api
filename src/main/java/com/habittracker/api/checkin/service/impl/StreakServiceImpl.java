package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.auth.utils.AuthUtils.getUserTimeZone;
import static com.habittracker.api.checkin.constants.StreakConstants.STREAK_CACHE_KEY_PREFIX;
import static com.habittracker.api.core.utils.TemporalUtils.isTodayOrYesterday;

import com.habittracker.api.auth.utils.AuthUtils;
import com.habittracker.api.checkin.dto.StreakCalculationResult;
import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.StreakCalculator;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakServiceImpl implements StreakService {

  private final CheckInRepository checkInRepository;
  private final HabitHelper habitHelper;
  private final RedisTemplate<String, Object> redisTemplate;
  private final StreakCalculator streakCalculator;

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, authentication.principal.id)")
  @Transactional(readOnly = true)
  public StreakResponse calculateStreak(UUID habitId) {
    habitHelper.ensureHabitNotDeleted(habitId);
    int currentStreak = getStreak(habitId, getUserTimeZone());
    return new StreakResponse(habitId, currentStreak, Instant.now());
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habit.id, authentication.principal.id)")
  @Transactional
  public void incrementStreak(HabitEntity habit) {
    ZoneId userTimeZone = getUserTimeZone();
    int currentStreak = getStreak(habit.getId(), userTimeZone);
    int newStreak = currentStreak + 1;

    updateBestStreakIfNeeded(habit, newStreak, userTimeZone);

    LocalDate today = LocalDate.now(userTimeZone);
    cacheStreak(habit.getId(), newStreak, today, userTimeZone);
    log.debug("Incremented streak for habit ID: {} to {}", habit.getId(), newStreak);
  }

  @Override
  public long getUserActiveStreaks(UUID userId) {

    return redisTemplate.keys(String.format("%s:%s:*", STREAK_CACHE_KEY_PREFIX, userId)).size();
  }

  private void updateBestStreakIfNeeded(HabitEntity habit, int newStreak, ZoneId userTimeZone) {
    if (newStreak <= habit.getBestStreak()) {
      return;
    }
    habit.setBestStreak(newStreak);
    habit.setBestStreakStartDate(LocalDate.now(userTimeZone).minusDays(newStreak -1));
  }

  private int getStreak(UUID habitId, ZoneId userTimeZone) {
    String cacheKey = buildStreakCacheKey(AuthUtils.getUserId(), habitId);

    Integer cachedStreak = (Integer) redisTemplate.opsForValue().get(cacheKey);
    if (cachedStreak != null) {
      log.debug("Streak cache hit for habit ID: {}", habitId);
      return cachedStreak;
    }

    log.debug("Streak cache miss for habit ID: {}", habitId);
    log.debug("Calculating streak for habit ID: {}", habitId);

    StreakCalculationResult result = calculateStreakFromDatabase(habitId, userTimeZone);
    cacheStreak(habitId, result.streak(), result.mostRecentCheckInDate(), userTimeZone);

    return result.streak();
  }

  private StreakCalculationResult calculateStreakFromDatabase(UUID habitId, ZoneId userTimeZone) {
    return checkInRepository
        .findFirstByHabitIdOrderByCreatedAtDesc(habitId)
        .map(
            mostRecent -> {
              LocalDate mostRecentDate =
                  mostRecent.getCreatedAt().atZone(userTimeZone).toLocalDate();
              int streak = calculateStreakIfActive(habitId, mostRecent, userTimeZone);
              return new StreakCalculationResult(streak, mostRecentDate);
            })
        .orElseGet(
            () -> {
              log.debug("No check-ins found for habit ID: {}, streak is 0", habitId);
              return new StreakCalculationResult(0, null);
            });
  }

  private int calculateStreakIfActive(
      UUID habitId, CheckInEntity mostRecentCheckIn, ZoneId userTimeZone) {
    LocalDate mostRecentDate = mostRecentCheckIn.getCreatedAt().atZone(userTimeZone).toLocalDate();

    if (!isTodayOrYesterday(mostRecentDate, userTimeZone)) {
      log.debug(
          "Most recent check-in for habit ID: {} is from {}, streak broken (not today/yesterday)",
          habitId,
          mostRecentDate);
      return 0;
    }

    log.debug("Most recent check-in is active, fetching all check-ins for calculation");
    List<CheckInEntity> allCheckIns = checkInRepository.findByHabitIdOrderByCreatedAtDesc(habitId);
    return streakCalculator.calculateCurrentStreak(allCheckIns, userTimeZone);
  }

  private void cacheStreak(
      UUID habitId, int streak, LocalDate mostRecentCheckInDate, ZoneId userTimeZone) {
    if (mostRecentCheckInDate == null) {
      log.debug("Skipping cache for habit ID: {} - no check-ins exist", habitId);
      return;
    }

    String cacheKey = buildStreakCacheKey(AuthUtils.getUserId(), habitId);
    int daysUntilExpiry = calculateDaysUntilExpiry(mostRecentCheckInDate, userTimeZone);

    LocalDateTime midnight =
        LocalDateTime.now(userTimeZone).truncatedTo(ChronoUnit.DAYS).plusDays(daysUntilExpiry);
    Instant expireAt = midnight.atZone(userTimeZone).toInstant();

    redisTemplate.opsForValue().set(cacheKey, streak);
    redisTemplate.expireAt(cacheKey, expireAt);
  }

  private String buildStreakCacheKey(UUID userId, UUID habitId) {
    return String.format("%s%s:%s", STREAK_CACHE_KEY_PREFIX, userId, habitId);
  }

  private int calculateDaysUntilExpiry(LocalDate mostRecentCheckInDate, ZoneId userTimeZone) {
    LocalDate today = LocalDate.now(userTimeZone);

    if (mostRecentCheckInDate.isBefore(today)) {
      log.debug(
          "Last check-in was on {}, setting cache to expire tonight at midnight",
          mostRecentCheckInDate);
      return 1;
    }

    log.debug(
        "Last check-in was today ({}), setting cache to expire tomorrow at midnight",
        mostRecentCheckInDate);
    return 2;
  }
}
