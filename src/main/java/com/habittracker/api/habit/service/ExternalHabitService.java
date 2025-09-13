package com.habittracker.api.habit.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import java.util.UUID;

public interface ExternalHabitService {

  HabitResponse createHabit(UserEntity user, CreateHabitRequest request);

  PagedModel<HabitResponse> getUserHabits(UserEntity user, Pageable pageable);

  void delete(@NotNull UUID id, UUID userId);
}
