package com.habittracker.api.habit.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.service.ExternalHabitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@Slf4j
public class HabitController {

  private final ExternalHabitService externalHabitService;

  @PostMapping
  public ResponseEntity<HabitResponse> createHabit(
      @Valid @RequestBody CreateHabitRequest request,
      @AuthenticationPrincipal UserDetailsImpl principal) {

    log.info("Creating habit '{}' for user {}", request.name(), principal.getUser().getId());
    HabitResponse response = externalHabitService.createHabit(principal.getUser(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<PagedModel<HabitResponse>> getUserHabits(
          @AuthenticationPrincipal UserDetailsImpl principal, @PageableDefault(sort = "createdAt")
          Pageable pageable) {

    log.debug("Fetching habits for user {}", principal.getUser().getId());
    PagedModel<HabitResponse> habits = externalHabitService.getUserHabits(principal.getUser(), pageable);
    return ResponseEntity.ok(habits);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @NotNull @PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl principal) {
    externalHabitService.delete(id, principal.getUser().getId());
    return ResponseEntity.noContent().build();
  }
}
