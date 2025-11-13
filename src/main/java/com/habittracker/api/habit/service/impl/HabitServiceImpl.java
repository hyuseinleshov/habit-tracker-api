package com.habittracker.api.habit.service.impl;

import static com.habittracker.api.habit.specs.HabitSpecs.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.dto.UpdateHabitRequest;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.service.HabitService;
import com.habittracker.api.habit.streak.service.StreakService;
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
  private final UserRepository userRepository;
  private final HabitMapper habitMapper;
  private final HabitHelper habitHelper;
  private final StreakService streakService;

  @Override
  public HabitResponse createHabit(UUID userId, CreateHabitRequest request) {
    log.debug("Creating habit with name '{}' for user {}", request.name(), userId);

    // Check if habit name already exists for this user
    habitHelper.isUniqueName(userId, request.name());

    UserEntity user = userRepository.getReferenceById(userId);
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
        userId);

    return toResponse(savedHabit);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedModel<HabitResponse> getUserHabits(
      UUID userId, Pageable pageable, boolean isArchived) {
    log.debug("Fetching habits for user {}", userId);

    Page<HabitEntity> habits =
        habitRepository.findAll(
            hasUser(userId).and(isDeleted(false).and(isArchived(isArchived))), pageable);

    return new PagedModel<>(habits.map(this::toResponse));
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#id, principal.id)")
  public HabitResponse getById(UUID id) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(id);
    return toResponse(habit);
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#id, principal.id)")
  public void delete(UUID id) {
    HabitEntity toDelete = habitHelper.getNotDeletedOrThrow(id);
    toDelete.setDeletedAt(Instant.now());
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#id, #userId)")
  public HabitResponse update(UUID id, UUID userId, UpdateHabitRequest updateRequest) {
    HabitEntity toUpdate = habitHelper.getNotDeletedOrThrow(id);
    if (updateRequest.name() != null) habitHelper.isUniqueName(userId, updateRequest.name());
    return toResponse(habitMapper.updateHabitFromUpdateRequest(updateRequest, toUpdate));
  }

  private HabitResponse toResponse(HabitEntity entity) {
    StreakResponse streak = streakService.calculateStreak(entity.getId());
    return habitMapper.toResponse(entity, streak.currentStreak());
  }
}
