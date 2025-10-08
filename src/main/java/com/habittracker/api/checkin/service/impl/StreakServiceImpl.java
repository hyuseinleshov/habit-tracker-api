package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.constants.StreakConstants.STREAK_CACHE_KEY_PREFIX;
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
    String cacheKey = STREAK_CACHE_KEY_PREFIX + habitId;

    // 1. Check Redis cache (O(1) fast path - NO DB query!)
    Integer cachedStreak = (Integer) redisTemplate.opsForValue().get(cacheKey);
    if (cachedStreak != null) {
      log.debug("Streak cache hit for habit ID: {}", habitId);
      return new StreakResponse(habitId, cachedStreak, Instant.now());
    }

    // 2. Cache miss - calculate from database
    log.debug("Streak cache miss for habit ID: {}", habitId);
    log.debug("Calculating streak for habit ID: {}", habitId);

    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    ZoneId userTimeZone = parseTimeZone(habit.getUser().getUserProfile().getTimezone());

    // 3. Check if the most recent check-in is from today or yesterday
    CheckInEntity mostRecentCheckIn =
        checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(habitId).orElse(null);

    int currentStreak;
    if (mostRecentCheckIn == null) {
      currentStreak = 0;
    } else {
      LocalDate mostRecentDate =
          mostRecentCheckIn.getCreatedAt().atZone(userTimeZone).toLocalDate();
      LocalDate today = LocalDate.now(userTimeZone);
      LocalDate yesterday = today.minusDays(1);

      // Current streak exists only if most recent check-in is today or yesterday
      if (mostRecentDate.isEqual(today) || mostRecentDate.isEqual(yesterday)) {
        List<CheckInEntity> checkIns = checkInRepository.findByHabitIdOrderByCreatedAtDesc(habitId);
        currentStreak = calculateConsecutiveDays(checkIns, userTimeZone);
      } else {
        currentStreak = 0;
      }
    }

    // 4. Store in Redis with TTL until midnight of the day after tomorrow (user's timezone)
    Duration untilMidnight = calculateDurationUntilMidnight(userTimeZone);
    Duration cacheTtl = untilMidnight.plusDays(1);
    redisTemplate.opsForValue().set(cacheKey, currentStreak, cacheTtl);

    return new StreakResponse(habitId, currentStreak, Instant.now());
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
