package com.habittracker.api.habit.streak.service;

import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.streak.dto.BestStreakData;

import java.time.LocalDate;
import java.util.UUID;

public interface StreakService {

  /**
   * Calculates the current consecutive day streak for a habit.
   *
   * <p>Returns cached value if available, otherwise calculates from database. Streak counts
   * consecutive days backwards from the most recent check-in. Only active if last check-in was
   * today or yesterday.
   *
   * @param habitId the UUID of the habit
   * @return StreakResponse with current streak count and calculation timestamp
   */
  StreakResponse calculateStreak(UUID habitId);

  /**
   * Increments the streak for a habit by 1 after a new check-in.
   *
   * <p>Retrieves current streak via {@link #calculateStreak}, increments it, and updates the cache.
   *
   * @param habit the habit
   */
  void incrementStreak(HabitEntity habit);

  BestStreakData buildBestStreak(
          int streak, LocalDate streakStartDate, UUID habitId);
}
