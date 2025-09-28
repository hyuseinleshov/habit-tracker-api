package com.habittracker.api.checkin.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.mapper.CheckInMapper;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        habit, user.getId(), ZoneId.of(user.getUserProfile().getTimezone()));
    CheckInEntity checkInEntity = new CheckInEntity();
    checkInEntity.setHabit(habit);
    CheckInEntity saved = checkInRepository.save(checkInEntity);
    log.debug("Check in for habit with id {}.", habit.getId());
    return checkInMapper.toResponse(saved);
  }
}
