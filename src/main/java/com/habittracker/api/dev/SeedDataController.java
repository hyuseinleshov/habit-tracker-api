package com.habittracker.api.dev;

import com.habittracker.api.auth.model.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/seed")
@RequiredArgsConstructor
@Profile("dev")
public class SeedDataController {

  private final SeedDataService seedDataService;

  @PostMapping("/check-ins")
  public ResponseEntity<SeedResponse> seedCheckIns(
      @RequestParam(defaultValue = "50") int count,
      @RequestParam(defaultValue = "30") int daysBack,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    int created = seedDataService.seedCheckInsForUser(userDetails.id(), count, daysBack);
    return ResponseEntity.ok(new SeedResponse("Check-ins seeded successfully", created));
  }

  @PostMapping("/habits")
  public ResponseEntity<SeedResponse> seedHabits(
      @RequestParam(defaultValue = "10") int count,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    int created = seedDataService.seedHabitsForUser(userDetails.id(), count);
    return ResponseEntity.ok(new SeedResponse("Habits seeded successfully", created));
  }

  @PostMapping("/full")
  public ResponseEntity<SeedResponse> seedFull(
      @RequestParam(defaultValue = "5") int habitCount,
      @RequestParam(defaultValue = "100") int checkInCount,
      @RequestParam(defaultValue = "60") int daysBack,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    SeedSummary summary =
        seedDataService.seedFullDataForUser(userDetails.id(), habitCount, checkInCount, daysBack);
    return ResponseEntity.ok(
        new SeedResponse(
            String.format(
                "Full seed completed: %d habits, %d check-ins",
                summary.habitsCreated(), summary.checkInsCreated()),
            summary.habitsCreated() + summary.checkInsCreated()));
  }

  @DeleteMapping("/clear")
  public ResponseEntity<SeedResponse> clearUserData(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    int deleted = seedDataService.clearUserData(userDetails.id());
    return ResponseEntity.ok(new SeedResponse("User data cleared successfully", deleted));
  }
}
