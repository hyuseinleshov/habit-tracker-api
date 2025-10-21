package com.habittracker.api.habit.controller;

import com.habittracker.api.habit.dto.HabitStatisticResponse;
import com.habittracker.api.habit.service.HabitStatisticsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/habits/{habitId}/stats")
@RequiredArgsConstructor
public class HabitStatisticsController {

    private final HabitStatisticsService habitStatisticsService;

    @GetMapping
    public ResponseEntity<HabitStatisticResponse> getStatistics(@PathVariable @NonNull UUID habitId) {
        return ResponseEntity.ok(habitStatisticsService.calculateStatistics(habitId));
    }
}
