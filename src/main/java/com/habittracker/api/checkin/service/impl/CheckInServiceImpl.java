package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.specs.CheckInSpecs.*;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.mapper.CheckInMapper;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, #user.id)")
  @Transactional
  public CheckInResponse checkIn(UUID habitId, UserEntity user) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
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
  public Page<CheckInResponse> getCheckInsByHabit(
      UUID habitId, UserEntity user, Instant from, Instant to, Pageable pageable) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    Specification<CheckInEntity> spec = hasHabit(habit).and(createdAfter(from)).and(createdBefore(to));
    return checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CheckInWithHabitResponse> getAllCheckIns(
      UserEntity user, Instant from, Instant to, Pageable pageable) {
    Specification<CheckInEntity> spec = hasUser(user).and(createdAfter(from)).and(createdBefore(to));
    return checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponseWithHabit);
  }
}
