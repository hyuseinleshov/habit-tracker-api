package com.habittracker.api.habit.helpers;

import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HabitHelper {

  private final HabitRepository habitRepository;

  public HabitEntity getNotDeletedOrThrow(UUID id) {
    HabitEntity habit = habitRepository.findById(id).orElseThrow(HabitNotFoundException::new);
    if (habit.isDeleted()) {
      throw new HabitAlreadyDeletedException();
    }
    return habit;
  }

  public void isUniqueName(UUID userId, String habitName) {
    if (habitRepository.existsByNameIgnoreCaseAndUserId(habitName, userId)) {
      log.warn("Attempt to create habit with duplicate name '{}' for user {}", habitName, userId);
      throw new HabitNameAlreadyExistsException();
    }
  }

  public boolean isOwnedByUser(UUID id, UUID userId) {
    return habitRepository.existsByIdAndUserId(id, userId);
  }
}
