package com.habittracker.api.habit.service.impl;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_ALREADY_DELETED_MESSAGE;
import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_NULL_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.testutils.HabitTestUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InternalHabitServiceImplTest {

  private final InternalHabitServiceImpl toTest = new InternalHabitServiceImpl();

  private HabitEntity testHabitEntity;

  @BeforeEach
  void setUp() {
    UserEntity testUser =
        AuthTestUtils.createUser("test@gmail.com", "pass", AuthTestUtils.createUserRole());
    testUser.setId(UUID.randomUUID());
    testHabitEntity = HabitTestUtils.createHabit(testUser, "Habit", "desc");
  }

  @Nested
  class SoftDeleteTests {

    @Test
    void shouldThrowIllegalArgument_WhenHabitIsNull() {
      assertThatThrownBy(() -> toTest.softDelete(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(HABIT_NOT_NULL_MESSAGE);
    }

    @Test
    void shouldThrowAlreadyDeleted_WhenHabitIsDeleted() {
      testHabitEntity.setDeletedAt(Instant.now());
      assertThatThrownBy(() -> toTest.softDelete(testHabitEntity))
          .isInstanceOf(HabitAlreadyDeletedException.class)
          .hasMessage(HABIT_ALREADY_DELETED_MESSAGE);
    }

    @Test
    void shouldDelete_WithValidHabit() {
      toTest.softDelete(testHabitEntity);
      Instant now = Instant.now();
      assertThat(testHabitEntity.isDeleted()).isTrue();
      assertThat(testHabitEntity.getDeletedAt()).isAfter(now.minus(3, ChronoUnit.SECONDS));
      assertThat(testHabitEntity.getDeletedAt()).isBefore(now.plus(3, ChronoUnit.SECONDS));
    }
  }
}
