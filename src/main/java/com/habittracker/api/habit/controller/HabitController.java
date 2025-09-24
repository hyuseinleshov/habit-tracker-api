package com.habittracker.api.habit.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.dto.UpdateHabitRequest;
import com.habittracker.api.habit.service.HabitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<PagedModel<HabitResponse>> getUserHabits(
      @AuthenticationPrincipal UserDetailsImpl principal,
      @PageableDefault(sort = "createdAt") Pageable pageable,
      @RequestParam(required = false, defaultValue = "false", name = "archived")
          boolean isArchived) {

    log.debug("Fetching habits for user {}", principal.getUser().getId());
    PagedModel<HabitResponse> habits =
        habitService.getUserHabits(principal.getUser(), pageable, isArchived);
    return ResponseEntity.ok(habits);
  }

  @GetMapping("/{id}")
  public ResponseEntity<HabitResponse> byId(@NotNull @PathVariable UUID id) {
    return ResponseEntity.ok(habitService.getById(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<HabitResponse> update(
      @NotNull @PathVariable UUID id, @Valid @RequestBody UpdateHabitRequest updateRequest) {
    return ResponseEntity.ok(habitService.update(id, updateRequest));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@NotNull @PathVariable UUID id) {
    habitService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
