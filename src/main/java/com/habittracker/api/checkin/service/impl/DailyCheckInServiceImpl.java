package com.habittracker.api.checkin.service.impl;

import com.habittracker.api.auth.utils.AuthUtils;
import com.habittracker.api.checkin.exception.DuplicateCheckInException;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyCheckInServiceImpl implements DailyCheckInService {

  private final CheckInRepository checkInRepository;

  @Override
  @CacheEvict(value = "dailyCheckIn", key = "#userId + ':' + #habit.id")
  public void registerTodayCheckin(HabitEntity habit, UUID userId, ZoneId userTimeZone) {
    if (hasCheckedInToday(habit.getId(), userTimeZone)) {
      log.debug("Try to check in again today for habit with id {}", habit.getId());
      throw new DuplicateCheckInException(habit.getId());
    }
  }

  @Override
  @Cacheable(
      value = "dailyCheckIn",
      key = "T(com.habittracker.api.auth.utils.AuthUtils).getUserId() + ':' + #habitId")
  public boolean isCheckedInToday(UUID habitId) {
    ZoneId userTimeZone = AuthUtils.getUserTimeZone();
    return hasCheckedInToday(habitId, userTimeZone);
  }

  private boolean hasCheckedInToday(UUID habitId, ZoneId userTimeZone) {
    Instant startOfDay = LocalDate.now(userTimeZone).atStartOfDay(userTimeZone).toInstant();
    Instant endOfDay =
        LocalDate.now(userTimeZone).plusDays(1).atStartOfDay(userTimeZone).toInstant();
    return checkInRepository.existsByHabitIdAndCreatedAtBetween(habitId, startOfDay, endOfDay);
  }
}
