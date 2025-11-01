package com.habittracker.api.habit.service;

import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.dto.UpdateHabitRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface HabitService {

  HabitResponse createHabit(UUID userId, CreateHabitRequest request);

  PagedModel<HabitResponse> getUserHabits(UUID userId, Pageable pageable, boolean isArchived);

  void delete(@NotNull UUID id);

  HabitResponse getById(@NotNull UUID id);

  HabitResponse update(@NotNull UUID id, UUID userId, @Valid UpdateHabitRequest updateRequest);
}
