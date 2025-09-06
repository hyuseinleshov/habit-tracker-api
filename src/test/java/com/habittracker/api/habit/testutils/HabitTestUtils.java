package com.habittracker.api.habit.testutils;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HabitTestUtils {

  private final HabitRepository habitRepository;

  public HabitTestUtils(HabitRepository habitRepository) {
    this.habitRepository = habitRepository;
  }

  public static HabitEntity createHabit(UserEntity user, String name, String description) {
    HabitEntity habit = new HabitEntity();
    habit.setUser(user);
    habit.setName(name);
    habit.setDescription(description);
    habit.setArchived(false);
    return habit;
  }

  public HabitEntity createAndSaveHabit(UserEntity user, String name, String description) {
    return habitRepository.save(createHabit(user, name, description));
  }

  public HabitEntity createAndSaveHabit(UserEntity user, String name) {
    return createAndSaveHabit(user, name, "Default description for " + name);
  }

  @Transactional
  public void softDelete(HabitEntity habit, Instant deleteAt) {
    habit.setDeletedAt(deleteAt);
    habitRepository.save(habit);
  }
}
