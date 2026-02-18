package com.habittracker.api.habit.helpers;

import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.projections.HabitStatusProjection;
import com.habittracker.api.habit.repository.HabitRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HabitHelper {

  private final HabitRepository habitRepository;

  @Transactional
  public HabitEntity getNotDeletedOrThrow(UUID id) {
    ensureHabitNotDeleted(id);
    return habitRepository.getReferenceById(id);
  }

  public void ensureHabitNotDeleted(UUID id) {
    HabitStatusProjection habitStatus =
        habitRepository.findStatusById(id).orElseThrow(HabitNotFoundException::new);
    if (habitStatus.getDeletedAt() != null) {
      throw new HabitAlreadyDeletedException();
    }
  }

  public void isUniqueName(UUID userId, String habitName) {
    if (habitRepository.existsByNameIgnoreCaseAndDeletedAtIsNullAndUserId(habitName, userId)) {
      log.warn("Attempt to create habit with duplicate name '{}' for user {}", habitName, userId);
      throw new HabitNameAlreadyExistsException();
    }
  }

  public boolean isOwnedByUser(UUID id, UUID userId) {
    return habitRepository.existsByIdAndUserId(id, userId);
  }
}
