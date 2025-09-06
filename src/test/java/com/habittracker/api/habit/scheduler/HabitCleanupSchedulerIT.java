package com.habittracker.api.habit.scheduler;

import static com.habittracker.api.config.constants.AuthTestConstants.TEST_TIMEZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.testutils.HabitTestUtils;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@BaseIntegrationTest
class HabitCleanupSchedulerIT {

  private static final Map<Integer, String> DAYS_SINCE_DELETION_TO_NAME_MAP =
      Map.of(
          1,
          "Today Habit.",
          7,
          "Before week habit.",
          14,
          "Before two weeks habit.",
          30,
          "Before a month habit.",
          365,
          "Before a year habit.");

  @Autowired private HabitCleanupScheduler totest;

  @Autowired private HabitRepository habitRepository;
  @Autowired private AuthTestUtils authTestUtils;
  @Autowired private HabitTestUtils habitTestUtils;

  private UserEntity testUser;

  @Value("${habit.cleanup.retention-period}")
  private Period habitRetention;

  @BeforeEach
  void setUp() {
    this.testUser = authTestUtils.createAndSaveUser("tneohoeu@gmail.com", "pass", TEST_TIMEZONE);
  }

  @Test
  public void test_CleanupHabits_Deleted_Old_Habits() {
    setUpDeledHabits();
    long beforeCleanup = habitRepository.count();
    totest.cleanupHabits();
    List<String> deletedEmails =
        DAYS_SINCE_DELETION_TO_NAME_MAP.entrySet().stream()
            .filter(e -> e.getKey() >= habitRetention.getDays())
            .map(Map.Entry::getValue)
            .toList();
    assertEquals(beforeCleanup - deletedEmails.size(), habitRepository.count());
    deletedEmails.forEach(
        name -> assertFalse(habitRepository.existsByUserAndNameIgnoreCase(testUser, name)));
  }

  private void setUpDeledHabits() {
    DAYS_SINCE_DELETION_TO_NAME_MAP.forEach(
        (days, name) -> deleteHabit(name, Instant.now().minus(days, ChronoUnit.DAYS)));
  }

  private void deleteHabit(String name, Instant deletedAt) {
    HabitEntity savedHabit = habitTestUtils.createAndSaveHabit(testUser, name);
    habitTestUtils.softDelete(savedHabit, deletedAt);
    habitRepository.save(savedHabit);
  }
}
