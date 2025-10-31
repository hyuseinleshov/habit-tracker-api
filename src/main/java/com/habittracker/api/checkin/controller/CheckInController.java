package com.habittracker.api.checkin.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.service.CheckInService;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CheckInController {

  private final CheckInService checkInService;

  @PostMapping("/api/habits/{id}/check-ins")
  public ResponseEntity<CheckInResponse> checkIn(
      @NotNull @PathVariable("id") UUID habitId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.status(CREATED).body(checkInService.checkIn(habitId, userDetails.id()));
  }

  @GetMapping("/api/habits/{id}/check-ins")
  public ResponseEntity<PagedModel<CheckInResponse>> getCheckInsByHabit(
      @NotNull @PathVariable("id") UUID habitId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.ok(
        checkInService.getCheckInsByHabit(habitId, userDetails.id(), from, to, pageable));
  }

  @GetMapping("/api/check-ins")
  public ResponseEntity<PagedModel<CheckInWithHabitResponse>> getAllCheckIns(
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.ok(checkInService.getAllCheckIns(userDetails.id(), from, to, pageable));
  }

  @DeleteMapping("/api/check-ins/{checkInId}")
  public ResponseEntity<Void> deleteCheckIn(
      @NotNull @PathVariable UUID checkInId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
    checkInService.deleteCheckIn(checkInId, userDetails.id());
    return ResponseEntity.noContent().build();
  }
}
