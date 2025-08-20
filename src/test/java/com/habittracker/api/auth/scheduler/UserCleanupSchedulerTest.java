package com.habittracker.api.auth.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

import com.habittracker.api.auth.repository.UserRepository;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCleanupSchedulerTest {

  @Mock private UserRepository userRepository;

  private UserCleanupScheduler toTest;

  private static final Period testUserPeriod = Period.of(0, 0, 2);

  @BeforeEach
  void setUp() {
    this.toTest = new UserCleanupScheduler(userRepository, testUserPeriod);
  }

  @Test
  public void test_CleanUsers_Call_CorrectMethod_With_Valid_Arguments() {
    Instant now = Instant.now();
    toTest.cleanupUsers();
    ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(userRepository).deleteAllByDeletedAtBefore(instantArgumentCaptor.capture());
    Instant deleteBefore = instantArgumentCaptor.getValue();
    assertThat(deleteBefore).isBefore(now.minus(testUserPeriod).plus(3, ChronoUnit.SECONDS));
  }
}
