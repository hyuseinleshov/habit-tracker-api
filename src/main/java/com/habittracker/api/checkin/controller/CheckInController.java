package com.habittracker.api.checkin.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.service.CheckInService;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/habits/{id}/check-ins")
@RequiredArgsConstructor
public class CheckInController {

  private final CheckInService checkInService;

  @PostMapping
  public ResponseEntity<CheckInResponse> checkIn(
      @NotNull @PathVariable("id") UUID habitId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.status(CREATED)
        .body(checkInService.checkIn(habitId, userDetails.getUser()));
  }
}
