package com.habittracker.api.checkin.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface CheckInService {

  CheckInResponse checkIn(UUID habitId, UserEntity user);

  PagedModel<CheckInResponse> getCheckInsByHabit(
      UUID habitId, UserEntity user, Instant from, Instant to, Pageable pageable);

  PagedModel<CheckInWithHabitResponse> getAllCheckIns(
      UserEntity user, Instant from, Instant to, Pageable pageable);

  void deleteCheckIn(UUID checkInId, UserEntity user);
}
