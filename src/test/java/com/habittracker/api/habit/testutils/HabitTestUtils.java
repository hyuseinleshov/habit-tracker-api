package com.habittracker.api.habit.testutils;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import org.springframework.stereotype.Component;

@Component
public class HabitTestUtils {

  private final HabitRepository habitRepository;

  public HabitTestUtils(HabitRepository habitRepository) {
    this.habitRepository = habitRepository;
  }

  public HabitEntity createAndSaveHabit(UserEntity user, String name, String description) {
    HabitEntity habit = new HabitEntity();
    habit.setUser(user);
    habit.setName(name);
    habit.setDescription(description);
    habit.setArchived(false);
    return habitRepository.save(habit);
  }

  public HabitEntity createAndSaveHabit(UserEntity user, String name) {
    return createAndSaveHabit(user, name, "Default description for " + name);
  }
}
