package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.constants.StreakConstants.STREAK_CACHE_KEY_PREFIX;
import static com.habittracker.api.core.utils.TimeZoneUtils.calculateDurationUntilMidnight;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.StreakCalculator;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Duration;
import java.time.Instant;
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
  private final StreakCalculator streakCalculator;

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

    // 4. Store in Redis with TTL until midnight tomorrow (user's timezone)
    cacheStreak(habitId, currentStreak, userTimeZone);

    return currentStreak;
  }

  private int calculateStreakFromDatabase(UUID habitId, ZoneId userTimeZone) {
    List<CheckInEntity> checkIns = checkInRepository.findByHabitIdOrderByCreatedAtDesc(habitId);
    return streakCalculator.calculateConsecutiveStreak(checkIns, userTimeZone);
  }

  private void cacheStreak(UUID habitId, int streak, ZoneId userTimeZone) {
    String cacheKey = STREAK_CACHE_KEY_PREFIX + habitId;
    // Cache expires at midnight tomorrow (user's timezone)
    // This aligns with streak expiration: user has until end of tomorrow to check in
    Duration cacheTtl = calculateDurationUntilMidnight(userTimeZone, 2);
    redisTemplate.opsForValue().set(cacheKey, streak, cacheTtl);
  }
}
