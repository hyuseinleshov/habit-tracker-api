package com.habittracker.api.auth.scheduler;

import static com.habittracker.api.config.constants.AuthTestConstants.TEST_PASSWORD;
import static com.habittracker.api.config.constants.AuthTestConstants.TEST_TIMEZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@BaseIntegrationTest
class UserCleanupSchedulerIT {

  private static final Map<Integer, String> DAYS_SINCE_DELETION_TO_EMAIL_MAP =
      Map.of(
          1,
          "yesterday@gmail.com",
          7,
          "week@gmail.com",
          14,
          "twoWeeks@gmail.com",
          30,
          "moth@gmail.com",
          365,
          "year@gmail.com");

  @Autowired private UserCleanupScheduler totest;

  @Autowired private UserRepository userRepository;
  @Autowired private AuthTestUtils authTestUtils;

  @Value("${user.cleanup.retention-period}")
  private Period userRetention;

  @Test
  public void test_CleanupUsers_Deleted_Old_Users() {
    setUpDeledUsers();
    long beforeCleanup = userRepository.count();
    totest.cleanupUsers();
    List<String> deletedEmails =
        DAYS_SINCE_DELETION_TO_EMAIL_MAP.entrySet().stream()
            .filter(e -> e.getKey() >= userRetention.getDays())
            .map(Map.Entry::getValue)
            .toList();
    assertEquals(beforeCleanup - deletedEmails.size(), userRepository.count());
    deletedEmails.forEach(email -> assertTrue(userRepository.findByEmail(email).isEmpty()));
  }

  private void setUpDeledUsers() {
    DAYS_SINCE_DELETION_TO_EMAIL_MAP.forEach(
        (days, email) -> deleteUser(email, Instant.now().minus(days, ChronoUnit.DAYS)));
  }

  private void deleteUser(String email, Instant deletedAt) {
    UserEntity savedUser = authTestUtils.createAndSaveUser(email, TEST_PASSWORD, TEST_TIMEZONE);
    authTestUtils.softDelete(savedUser, deletedAt);
    userRepository.save(savedUser);
  }
}
