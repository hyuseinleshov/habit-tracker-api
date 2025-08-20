package com.habittracker.api.auth.scheduler;

import com.habittracker.api.auth.repository.UserRepository;
import java.time.Instant;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class UserCleanupScheduler {

  private final UserRepository userRepository;
  private final Period userRetention;

  public UserCleanupScheduler(
      UserRepository userRepository,
      @Value("${user.cleanup.retention-period}") Period userRetention) {
    this.userRepository = userRepository;
    this.userRetention = userRetention;
  }

  @Scheduled(cron = "${user.cleanup.schedule.cron}")
  @Transactional
  public void cleanupUsers() {
    Instant deleteBefore = Instant.now().minus(userRetention);
    long deletedCount = userRepository.deleteAllByDeletedAtBefore(deleteBefore);
    log.info(
        "Cleaned up {} user(s) that were deleted more than {} days ago.",
        deletedCount,
        userRetention.getDays());
  }
}
