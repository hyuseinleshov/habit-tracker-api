package com.habittracker.api.checkin.service.impl;

import static com.habittracker.api.checkin.specs.CheckInSpecs.*;

import com.habittracker.api.auth.utils.AuthUtils;
import com.habittracker.api.checkin.dto.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.exception.CheckInNotFoundException;
import com.habittracker.api.checkin.mapper.CheckInMapper;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.CheckInService;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.streak.service.StreakService;
import java.time.*;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, #userId)")
  @Caching(
      evict = {
        @CacheEvict(value = "habitStatistics", key = "#habitId"),
        @CacheEvict(
            value = {"userStatistics", "weeklySummary"},
            key = "#userId"),
      })
  @Transactional
  public CheckInResponse checkIn(UUID habitId, UUID userId) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    streakService.incrementStreak(habit);

    dailyCheckInService.registerTodayCheckin(habit, userId, AuthUtils.getUserTimeZone());

    CheckInEntity checkInEntity = new CheckInEntity();
    checkInEntity.setHabit(habit);
    CheckInEntity saved = checkInRepository.save(checkInEntity);

    log.debug("Check in for habit with id {}.", habit.getId());
    return checkInMapper.toResponse(saved);
  }

  @Override
  @PreAuthorize("@habitHelper.isOwnedByUser(#habitId, principal.id())")
  @Transactional(readOnly = true)
  public PagedModel<CheckInResponse> getCheckInsByHabit(
      UUID habitId, Instant from, Instant to, Pageable pageable) {
    HabitEntity habit = habitHelper.getNotDeletedOrThrow(habitId);
    Specification<CheckInEntity> spec = buildSpecificationWithDateRange(hasHabit(habit), from, to);
    Page<CheckInResponse> page =
        checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponse);
    return new PagedModel<>(page);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedModel<CheckInWithHabitResponse> getAllCheckIns(
      UUID userId, Instant from, Instant to, Pageable pageable) {
    Specification<CheckInEntity> spec = buildSpecificationWithDateRange(hasUser(userId), from, to);
    Page<CheckInWithHabitResponse> page =
        checkInRepository.findAll(spec, pageable).map(checkInMapper::toResponseWithHabit);
    return new PagedModel<>(page);
  }

  @Override
  public long getHabitCheckInsCount(UUID habitId) {
    return checkInRepository.countByHabitId(habitId);
  }

  @Override
  public long getUserCheckInsCount(UUID userId) {
    return checkInRepository.countByHabitUserId(userId);
  }

  @Override
  public LocalDate getHabitLastCheckInDate(UUID habitId) {
    return checkInRepository
        .findFirstByHabitIdOrderByCreatedAtDesc(habitId)
        .map(CheckInEntity::getCreatedAt)
        .map(instant -> instant.atZone(AuthUtils.getUserTimeZone()).toLocalDate())
        .orElse(null);
  }

  @Override
  public LocalDate getUserLastCheckInDate(UUID userId) {
    return checkInRepository
        .findFirstByHabitUserIdOrderByCreatedAtDesc(userId)
        .map(CheckInEntity::getCreatedAt)
        .map(instant -> instant.atZone(AuthUtils.getUserTimeZone()).toLocalDate())
        .orElse(null);
  }

  @Override
  @PreAuthorize("@checkInHelper.isOwnedByUser(#checkInId, principal.id())")
  @Transactional
  public void deleteCheckIn(UUID checkInId) {
    CheckInEntity checkIn =
        checkInRepository.findById(checkInId).orElseThrow(CheckInNotFoundException::new);
    UUID habitId = checkIn.getHabit().getId();
    checkInRepository.delete(checkIn);

    // TODO
    // FIX
    streakService.calculateStreak(habitId);
  }

  @Override
  public long getCheckInsToday(UUID userId) {
    ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(AuthUtils.getUserTimeZone());
    ZonedDateTime endOfDay = startOfDay.plusDays(1);
    return checkInRepository
        .findByHabitUserIdAndCreatedAtBetween(userId, startOfDay.toInstant(), endOfDay.toInstant())
        .size();
  }

  @Override
  public Set<CheckInEntity> getCheckInsFor(UUID userId, LocalDate startDate, LocalDate endDate) {
    ZoneId userTimeZone = AuthUtils.getUserTimeZone();
    return checkInRepository.findByHabitUserIdAndCreatedAtBetween(
        userId,
        startDate.atStartOfDay(userTimeZone).toInstant(),
        endDate.plusDays(1).atStartOfDay(userTimeZone).toInstant());
  }

  private Specification<CheckInEntity> buildSpecificationWithDateRange(
      Specification<CheckInEntity> baseSpec, Instant from, Instant to) {
    return baseSpec.and(createdAfter(from)).and(createdBefore(to));
  }
}
