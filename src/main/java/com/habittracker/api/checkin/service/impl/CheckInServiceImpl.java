package com.habittracker.api.checkin.service.impl;

import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.mapper.CheckInMapper;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

  private final CheckInRepository checkInRepository;
  private final HabitHelper habitHelper;
  private final CheckInMapper checkInMapper;
  private final DailyCheckInService dailyCheckInService;

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, principal.id)")
  @Transactional
  public CheckInResponse checkIn(UUID habitId, String userTimezone) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    dailyCheckInService.registerTodayCheckin(habit, ZoneId.of(userTimezone));
    CheckInEntity checkInEntity = new CheckInEntity();
    checkInEntity.setHabit(habit);
    CheckInEntity saved = checkInRepository.save(checkInEntity);
    return checkInMapper.toResponse(saved);
  }
}
