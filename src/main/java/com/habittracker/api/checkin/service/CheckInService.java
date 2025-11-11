package com.habittracker.api.checkin.service;

import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface CheckInService {

  CheckInResponse checkIn(UUID habitId, UUID userId);

  PagedModel<CheckInResponse> getCheckInsByHabit(
      UUID habitId, Instant from, Instant to, Pageable pageable);

  PagedModel<CheckInWithHabitResponse> getAllCheckIns(
      UUID userId, Instant from, Instant to, Pageable pageable);

  long getHabitCheckInsCount(UUID habitId);

  long getUserCheckInsCount(UUID userId);

  LocalDate getHabitLastCheckInDate(UUID habitId);

  LocalDate getUserLastCheckInDate(UUID userId);

  void deleteCheckIn(UUID checkInId);

  long getCheckInsToday(UUID userId);

  Set<CheckInEntity> getCheckInsFor(UUID userId, LocalDate startDate, LocalDate endDate);
}
