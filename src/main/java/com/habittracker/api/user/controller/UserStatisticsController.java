package com.habittracker.api.user.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.user.dto.UserStatisticsResponse;
import com.habittracker.api.user.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats/overview")
@RequiredArgsConstructor
public class UserStatisticsController {

  private final UserStatisticsService statisticsService;

  @GetMapping
  public ResponseEntity<UserStatisticsResponse> getStatistics(
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(statisticsService.calculateStatistics(principal.id()));
  }
}
