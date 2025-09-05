package com.habittracker.api.habit.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.service.HabitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@Slf4j
public class HabitController {

  private final HabitService habitService;

  @PostMapping
  public ResponseEntity<HabitResponse> createHabit(
      @Valid @RequestBody CreateHabitRequest request,
      @AuthenticationPrincipal UserDetailsImpl principal) {

    log.info("Creating habit '{}' for user {}", request.name(), principal.getUser().getId());
    HabitResponse response = habitService.createHabit(principal.getUser(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<HabitResponse>> getUserHabits(
      @AuthenticationPrincipal UserDetailsImpl principal) {

    log.debug("Fetching habits for user {}", principal.getUser().getId());
    List<HabitResponse> habits = habitService.getUserHabits(principal.getUser());
    return ResponseEntity.ok(habits);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@NotNull @PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl principal) {
    habitService.delete(id, principal.getUser().getId());
    return ResponseEntity.noContent().build();
  }
}
