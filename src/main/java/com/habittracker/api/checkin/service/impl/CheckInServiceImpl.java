package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.specs.CheckInSpecs.*;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.exception.CheckInNotFoundException;
import com.habittracker.api.checkin.mapper.CheckInMapper;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.checkin.service.StreakService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInServiceImpl implements CheckInService {

  private final CheckInRepository checkInRepository;
  private final HabitHelper habitHelper;
  private final CheckInMapper checkInMapper;
  private final DailyCheckInService dailyCheckInService;
  private final StreakService streakService;

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, #user.id)")
  @Transactional
  public CheckInResponse checkIn(UUID habitId, UserEntity user) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    streakService.incrementStreak(habit);

    dailyCheckInService.registerTodayCheckin(
        habit, user.getId(), parseTimeZone(user.getUserProfile().getTimezone()));

    CheckInEntity checkInEntity = new CheckInEntity();
    checkInEntity.setHabit(habit);
    CheckInEntity saved = checkInRepository.save(checkInEntity);

    log.debug("Check in for habit with id {}.", habit.getId());
    return checkInMapper.toResponse(saved);
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, #user.id)")
  @Transactional(readOnly = true)
  public PagedModel<CheckInResponse> getCheckInsByHabit(
      UUID habitId, UserEntity user, Instant from, Instant to, Pageable pageable) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    Specification<CheckInEntity> spec = buildSpecificationWithDateRange(hasHabit(habit), from, to);
    Page<CheckInResponse> page =
        checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponse);
    return new PagedModel<>(page);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedModel<CheckInWithHabitResponse> getAllCheckIns(
      UserEntity user, Instant from, Instant to, Pageable pageable) {
    Specification<CheckInEntity> spec = buildSpecificationWithDateRange(hasUser(user), from, to);
    Page<CheckInWithHabitResponse> page =
        checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponseWithHabit);
    return new PagedModel<>(page);
  }

  @Override
  @PreAuthorize("@checkInHelper.isOwnedByUser(#checkInId, principal.id)")
  @Transactional
  public void deleteCheckIn(UUID checkInId, UserEntity user) {
    CheckInEntity checkIn =
        checkInRepository.findById(checkInId).orElseThrow(CheckInNotFoundException::new);
    UUID habitId = checkIn.getHabit().getId();
    checkInRepository.delete(checkIn);

    // For now, we just handle the deletion of check-in like this.
    // We will discuss if we will keep the delete functionality
    streakService.calculateStreak(habitId);

    log.debug("Deleted check-in with id {} for user {}", checkInId, user.getId());
  }

  private Specification<CheckInEntity> buildSpecificationWithDateRange(
      Specification<CheckInEntity> baseSpec, Instant from, Instant to) {
    return baseSpec.and(createdAfter(from)).and(createdBefore(to));
  }
}
