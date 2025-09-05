package com.habittracker.api.habit.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface HabitService {

  HabitResponse createHabit(UserEntity user, CreateHabitRequest request);

  List<HabitResponse> getUserHabits(UserEntity user);

    void delete(@NotNull UUID id, UUID userId);
}
