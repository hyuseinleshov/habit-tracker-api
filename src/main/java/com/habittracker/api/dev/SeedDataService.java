package com.habittracker.api.dev;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class SeedDataService {

  private final HabitRepository habitRepository;
  private final CheckInRepository checkInRepository;
  private final EntityManager entityManager;
  private final Random random = new Random();

  private static final String[] HABIT_NAMES = {
    "Morning Exercise",
    "Read 30 Minutes",
    "Drink Water",
    "Meditation",
    "Learn Programming",
    "Journal Writing",
    "Healthy Breakfast",
    "Evening Walk",
    "Practice Guitar",
    "Stretch Routine",
    "No Social Media",
    "Study Spanish",
    "Meal Prep",
    "Gratitude Practice",
    "Early Sleep"
  };

  private static final String[] HABIT_DESCRIPTIONS = {
    "Building a consistent habit",
    "Daily practice for self-improvement",
    "Making progress every day",
    "Focusing on personal growth",
    "Developing a healthy routine",
    null, // Some habits without description
    "Tracking my progress",
    "Making this a daily ritual"
  };

  @Transactional
  public int seedHabitsForUser(UserEntity user, int count) {
    log.info("Seeding {} habits for user {}", count, user.getEmail());

    List<HabitEntity> habits = new ArrayList<>();
    for (int i = 0; i < count && i < HABIT_NAMES.length; i++) {
      HabitEntity habit = new HabitEntity();
      habit.setUser(user);
      habit.setName(HABIT_NAMES[i] + " " + System.currentTimeMillis() % 10000);
      habit.setDescription(HABIT_DESCRIPTIONS[random.nextInt(HABIT_DESCRIPTIONS.length)]);
      habit.setArchived(random.nextInt(10) == 0); // 10% chance of archived
      habits.add(habit);
    }

    habitRepository.saveAll(habits);
    log.info("Created {} habits", habits.size());
    return habits.size();
  }

  @Transactional
  public int seedCheckInsForUser(UserEntity user, int count, int daysBack) {
    log.info("Seeding {} check-ins for user {} over {} days", count, user.getEmail(), daysBack);

    List<HabitEntity> userHabits =
        habitRepository.findAll().stream()
            .filter(h -> h.getUser().getId().equals(user.getId()) && h.getDeletedAt() == null)
            .toList();

    if (userHabits.isEmpty()) {
      log.warn("No habits found for user. Creating some first.");
      seedHabitsForUser(user, 5);
      userHabits =
          habitRepository.findAll().stream()
              .filter(h -> h.getUser().getId().equals(user.getId()))
              .toList();
    }

    List<CheckInEntity> checkIns = new ArrayList<>();
    Instant now = Instant.now();

    for (int i = 0; i < count; i++) {
      CheckInEntity checkIn = new CheckInEntity();
      checkIn.setHabit(userHabits.get(random.nextInt(userHabits.size())));
      checkIns.add(checkIn);
    }

    // Save all check-ins first
    List<CheckInEntity> saved = checkInRepository.saveAll(checkIns);
    entityManager.flush(); // Ensure all entities are persisted before updating

    // Update createdAt to spread over time range using native SQL
    saved.forEach(
        checkIn -> {
          // Random time within the past daysBack days
          long randomDays = random.nextInt(daysBack);
          long randomHours = random.nextInt(24);
          long randomMinutes = random.nextInt(60);

          Instant randomTime =
              now.minus(randomDays, ChronoUnit.DAYS)
                  .minus(randomHours, ChronoUnit.HOURS)
                  .minus(randomMinutes, ChronoUnit.MINUTES);

          // Use native SQL to update createdAt (bypassing @CreationTimestamp)
          entityManager
              .createNativeQuery("UPDATE check_ins SET created_at = :createdAt WHERE id = :id")
              .setParameter("createdAt", randomTime)
              .setParameter("id", checkIn.getId())
              .executeUpdate();
        });

    entityManager.flush();
    entityManager.clear(); // Clear persistence context to reload fresh data

    log.info("Created {} check-ins spread over {} days", saved.size(), daysBack);
    return saved.size();
  }

  @Transactional
  public SeedSummary seedFullDataForUser(
      UserEntity user, int habitCount, int checkInCount, int daysBack) {
    log.info(
        "Seeding full data for user {}: {} habits, {} check-ins over {} days",
        user.getEmail(),
        habitCount,
        checkInCount,
        daysBack);

    int habitsCreated = seedHabitsForUser(user, habitCount);
    int checkInsCreated = seedCheckInsForUser(user, checkInCount, daysBack);

    return new SeedSummary(habitsCreated, checkInsCreated);
  }

  @Transactional
  public int clearUserData(UserEntity user) {
    log.info("Clearing all data for user {}", user.getEmail());

    List<HabitEntity> userHabits =
        habitRepository.findAll().stream()
            .filter(h -> h.getUser().getId().equals(user.getId()))
            .toList();

    int checkInsDeleted = 0;
    for (HabitEntity habit : userHabits) {
      checkInsDeleted += habit.getCheckIns().size();
      checkInRepository.deleteAll(habit.getCheckIns());
    }

    int habitsDeleted = userHabits.size();
    habitRepository.deleteAll(userHabits);

    log.info("Deleted {} habits and {} check-ins", habitsDeleted, checkInsDeleted);
    return habitsDeleted + checkInsDeleted;
  }
}
