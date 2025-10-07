package com.habittracker.api.checkin.service;

import com.habittracker.api.checkin.dto.StreakResponse;
import java.util.UUID;

public interface StreakService {

  /**
   * Calculate the current streak for a given habit with Redis caching.
   *
   * <p>The streak represents consecutive days from the most recent check-in backwards without any
   * gaps. The calculation is timezone-aware, using the habit owner's timezone for date
   * calculations.
   *
   * <p>Caching Strategy: 1. First checks Redis cache for existing streak (O(1) operation) 2. On
   * cache hit, returns cached value immediately 3. On cache miss, calculates from database and
   * caches result 4. Cache expires at midnight of the day after tomorrow in the user's timezone
   *
   * <p>Calculation Algorithm (on cache miss): 1. Retrieve all check-ins for the habit ordered by
   * creation date (descending) 2. Convert each check-in timestamp to a local date in the user's
   * timezone 3. Starting from the most recent check-in, count consecutive days backwards 4. A
   * streak is broken when there's a gap of more than one day between check-ins
   *
   * <p>Example: - Check-ins on: Jan 5, Jan 4, Jan 3, Jan 1 → Streak = 3 (Jan 5, 4, 3) - Check-ins
   * on: Jan 5, Jan 3, Jan 2, Jan 1 → Streak = 1 (Jan 5 only) - No check-ins → Streak = 0
   *
   * @param habitId the UUID of the habit to calculate the streak for
   * @return StreakResponse containing the current streak count and calculation timestamp
   */
  StreakResponse calculateStreak(UUID habitId);
}
