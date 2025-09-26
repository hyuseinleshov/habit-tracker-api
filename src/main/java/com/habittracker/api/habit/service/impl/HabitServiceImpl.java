package com.habittracker.api.habit.service.impl;

import static com.habittracker.api.habit.specs.HabitSpecs.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.dto.UpdateHabitRequest;
import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.service.HabitService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HabitServiceImpl implements HabitService {

  private final HabitRepository habitRepository;
  private final HabitMapper habitMapper;

  @Override
  public HabitResponse createHabit(UserEntity user, CreateHabitRequest request) {
    log.debug("Creating habit with name '{}' for user {}", request.name(), user.getId());

    // Check if habit name already exists for this user
    isUniqueName(user.getId(), request.name());

    HabitEntity habit = new HabitEntity();
    habit.setUser(user);
    habit.setName(request.name().trim());
    habit.setDescription(request.description() != null ? request.description().trim() : null);
    habit.setArchived(false);

    HabitEntity savedHabit = habitRepository.save(habit);
    log.info(
        "Created habit '{}' with ID {} for user {}",
        savedHabit.getName(),
        savedHabit.getId(),
        user.getId());

    return habitMapper.toResponse(savedHabit);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedModel<HabitResponse> getUserHabits(
      UserEntity user, Pageable pageable, boolean isArchived) {
    log.debug("Fetching habits for user {}", user.getId());

    Page<HabitEntity> habits =
        habitRepository.findAll(
            hasUser(user).and(isDeleted(false).and(isArchived(isArchived))), pageable);

    return new PagedModel<>(habits.map(habitMapper::toResponse));
  }

  @Override
  @PreAuthorize("@habitServiceImpl.isOwnedByUser(#id, principal.id)")
  public HabitResponse getById(UUID id) {
    HabitEntity habit = getNotDeletedOrThrow(id);
    return habitMapper.toResponse(habit);
  }

  @Override
  @PreAuthorize("@habitServiceImpl.isOwnedByUser(#id, principal.id)")
  public void delete(UUID id) {
    HabitEntity toDelete = getNotDeletedOrThrow(id);
    toDelete.setDeletedAt(Instant.now());
  }

  @Override
  @PreAuthorize("@habitServiceImpl.isOwnedByUser(#id, #userId)")
  public HabitResponse update(UUID id, UUID userId, UpdateHabitRequest updateRequest) {
    HabitEntity toUpdate = getNotDeletedOrThrow(id);
    if (updateRequest.name() != null) isUniqueName(userId, updateRequest.name());
    return habitMapper.toResponse(
        habitMapper.updateHabitFromUpdateRequest(updateRequest, toUpdate));
  }

  private HabitEntity getNotDeletedOrThrow(UUID id) {
    HabitEntity habit = habitRepository.findById(id).orElseThrow(HabitNotFoundException::new);
    if (habit.isDeleted()) {
      throw new HabitAlreadyDeletedException();
    }
    return habit;
  }

  private void isUniqueName(UUID userId, String habitName) {
    if (habitRepository.existsByNameIgnoreCaseAndUserId(habitName, userId)) {
      log.warn("Attempt to create habit with duplicate name '{}' for user {}", habitName, userId);
      throw new HabitNameAlreadyExistsException();
    }
  }

  public boolean isOwnedByUser(UUID id, UUID userId) {
    return habitRepository.existsByIdAndUserId(id, userId);
  }
}
