package com.habittracker.api.habit.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.service.ExternalHabitService;
import com.habittracker.api.habit.service.InternalHabitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExternalHabitServiceImpl implements ExternalHabitService {

  private final HabitRepository habitRepository;
  private final InternalHabitService internalHabitService;
  private final HabitMapper habitMapper;

  @Override
  public HabitResponse createHabit(UserEntity user, CreateHabitRequest request) {
    log.debug("Creating habit with name '{}' for user {}", request.name(), user.getId());

    // Check if habit name already exists for this user
    if (habitRepository.existsByUserAndNameIgnoreCase(user, request.name())) {
      log.warn(
          "Attempt to create habit with duplicate name '{}' for user {}",
          request.name(),
          user.getId());
      throw new HabitNameAlreadyExistsException();
    }

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
  public PagedModel<HabitResponse> getUserHabits(UserEntity user, Pageable pageable) {
    log.debug("Fetching habits for user {}", user.getId());

    Page<HabitEntity> habits =
        habitRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable);

    return new PagedModel<>(habits.map(habitMapper::toResponse));
  }

  @Override
  public void delete(UUID id, UUID userId) {
    HabitEntity toDelete = habitRepository.findById(id).orElseThrow(HabitNotFoundException::new);
    internalHabitService.softDelete(toDelete);
  }
}
