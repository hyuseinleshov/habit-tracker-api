package com.habittracker.api.habit.scheduler;

import com.habittracker.api.habit.repository.HabitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Period;

@Component
@Slf4j
public class HabitCleanupScheduler {

  private final HabitRepository habitRepository;
  private final Period habitRetention;

  public HabitCleanupScheduler(
      HabitRepository habitRepository,
      @Value("${habit.cleanup.retention-period}") Period habitRetention) {
    this.habitRepository = habitRepository;
    this.habitRetention = habitRetention;
  }

  @Scheduled(cron = "${habit.cleanup.schedule.cron}")
  @Transactional
  public void cleanupHabits() {
    Instant deleteBefore = Instant.now().minus(habitRetention);
    long deletedCount = habitRepository.deleteAllByDeletedAtBefore(deleteBefore);
    log.info(
        "Cleaned up {} habit(s) that were deleted more than {} days ago.",
        deletedCount,
        habitRetention.getDays());
  }
}
