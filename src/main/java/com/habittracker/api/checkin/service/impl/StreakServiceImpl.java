package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.constants.StreakConstants.STREAK_CACHE_KEY_PREFIX;
import static com.habittracker.api.core.utils.TemporalUtils.isTodayOrYesterday;
import static com.habittracker.api.core.utils.TimeZoneUtils.calculateDurationUntilMidnight;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakServiceImpl implements StreakService {

  private final CheckInRepository checkInRepository;
  private final HabitHelper habitHelper;
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  @Transactional(readOnly = true)
  public StreakResponse calculateStreak(UUID habitId) {
    int currentStreak = getOrCalculateStreak(habitId);
    return new StreakResponse(habitId, currentStreak, Instant.now());
  }

  @Override
  public void incrementStreak(UUID habitId) {
    int currentStreak = getOrCalculateStreak(habitId);
    int newStreak = currentStreak + 1;

    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    ZoneId userTimeZone = parseTimeZone(habit.getUser().getUserProfile().getTimezone());

    cacheStreak(habitId, newStreak, userTimeZone);
    log.debug("Incremented streak for habit ID: {} to {}", habitId, newStreak);
  }

  private int getOrCalculateStreak(UUID habitId) {
    String cacheKey = STREAK_CACHE_KEY_PREFIX + habitId;

    // 1. Check Redis cache (O(1) fast path - NO DB query!)
    Integer cachedStreak = (Integer) redisTemplate.opsForValue().get(cacheKey);
    if (cachedStreak != null) {
      log.debug("Streak cache hit for habit ID: {}", habitId);
      return cachedStreak;
    }

    // 2. Cache miss - calculate from database
    log.debug("Streak cache miss for habit ID: {}", habitId);
    log.debug("Calculating streak for habit ID: {}", habitId);

    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    ZoneId userTimeZone = parseTimeZone(habit.getUser().getUserProfile().getTimezone());

    // 3. Calculate streak from database
    int currentStreak = calculateStreakFromDatabase(habitId, userTimeZone);

    // 4. Store in Redis with TTL until midnight of the day after tomorrow (user's timezone)
    cacheStreak(habitId, currentStreak, userTimeZone);

    return currentStreak;
  }

  private int calculateStreakFromDatabase(UUID habitId, ZoneId userTimeZone) {
    List<CheckInEntity> checkIns = checkInRepository.findByHabitIdOrderByCreatedAtDesc(habitId);

    if (checkIns.isEmpty()) {
      return 0;
    }

    LocalDate mostRecentDate = checkIns.get(0).getCreatedAt().atZone(userTimeZone).toLocalDate();

    if (!isTodayOrYesterday(mostRecentDate, userTimeZone)) {
      return 0;
    }

    return calculateConsecutiveDays(checkIns, userTimeZone);
  }

  private void cacheStreak(UUID habitId, int streak, ZoneId userTimeZone) {
    String cacheKey = STREAK_CACHE_KEY_PREFIX + habitId;
    // Cache expires at midnight of the day after tomorrow (user's timezone)
    // This gives users until end of tomorrow to maintain their streak
    Duration cacheTtl = calculateDurationUntilDayAfterTomorrowMidnight(userTimeZone);
    redisTemplate.opsForValue().set(cacheKey, streak, cacheTtl);
  }

  private Duration calculateDurationUntilDayAfterTomorrowMidnight(ZoneId userTimeZone) {
    Duration untilMidnight = calculateDurationUntilMidnight(userTimeZone);
    return untilMidnight.plusDays(1);
  }

  private int calculateConsecutiveDays(List<CheckInEntity> checkIns, ZoneId userTimeZone) {
    int streak = 1;
    LocalDate previousDate = checkIns.getFirst().getCreatedAt().atZone(userTimeZone).toLocalDate();

    for (int i = 1; i < checkIns.size(); i++) {
      LocalDate currentDate = checkIns.get(i).getCreatedAt().atZone(userTimeZone).toLocalDate();

      // Check if current date is exactly one day before previous date
      if (currentDate.plusDays(1).isEqual(previousDate)) {
        streak++;
        previousDate = currentDate;
      } else {
        // Gap found, streak is broken
        break;
      }
    }

    return streak;
  }
}
