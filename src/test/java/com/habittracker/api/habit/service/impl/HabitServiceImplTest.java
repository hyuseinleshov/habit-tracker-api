package com.habittracker.api.habit.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.Frequency;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.service.InternalHabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_ALREADY_DELETED_MESSAGE;
import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;
import static com.habittracker.api.habit.constants.HabitTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

  @Mock private HabitRepository habitRepository;
  @Mock private HabitMapper habitMapper;
  @Mock private InternalHabitService internalHabitService;
  @InjectMocks private HabitServiceImpl habitService;

  private UserEntity testUser;
  private CreateHabitRequest validRequest;
  private HabitEntity testHabitEntity;
  private HabitResponse testHabitResponse;

  @BeforeEach
  void setUp() {
    testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());

    validRequest = new CreateHabitRequest(HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_LONG);

    testHabitEntity = new HabitEntity();
    testHabitEntity.setId(UUID.randomUUID());
    testHabitEntity.setName(HABIT_NAME_READ_DAILY);
    testHabitEntity.setDescription(HABIT_DESCRIPTION_LONG);
    testHabitEntity.setUser(testUser);
    testHabitEntity.setArchived(EXPECTED_ARCHIVED);

    testHabitResponse =
        new HabitResponse(
            testHabitEntity.getId(),
            testHabitEntity.getName(),
            testHabitEntity.getDescription(),
            Frequency.DAILY,
            EXPECTED_ARCHIVED);
  }

  @Nested
  class CreateHabitTests {

    @Test
    void shouldCreateHabit_WhenValidRequest() {
      when(habitRepository.existsByUserAndNameIgnoreCase(testUser, validRequest.name()))
          .thenReturn(false);
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity)).thenReturn(testHabitResponse);

      HabitResponse result = habitService.createHabit(testUser, validRequest);

      assertThat(result).isEqualTo(testHabitResponse);

      ArgumentCaptor<HabitEntity> habitCaptor = ArgumentCaptor.forClass(HabitEntity.class);
      verify(habitRepository).save(habitCaptor.capture());

      HabitEntity savedHabit = habitCaptor.getValue();
      assertThat(savedHabit.getName()).isEqualTo(HABIT_NAME_READ_DAILY);
      assertThat(savedHabit.getDescription()).isEqualTo(HABIT_DESCRIPTION_LONG);
      assertThat(savedHabit.getUser()).isEqualTo(testUser);
      assertThat(savedHabit.isArchived()).isFalse();
    }

    @Test
    void shouldTrimWhitespace_WhenCreatingHabit() {
      CreateHabitRequest requestWithWhitespace =
          new CreateHabitRequest(HABIT_NAME_WHITESPACE, HABIT_DESCRIPTION_WHITESPACE);
      when(habitRepository.existsByUserAndNameIgnoreCase(testUser, HABIT_NAME_WHITESPACE))
          .thenReturn(false);
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity)).thenReturn(testHabitResponse);

      habitService.createHabit(testUser, requestWithWhitespace);

      ArgumentCaptor<HabitEntity> habitCaptor = ArgumentCaptor.forClass(HabitEntity.class);
      verify(habitRepository).save(habitCaptor.capture());

      HabitEntity savedHabit = habitCaptor.getValue();
      assertThat(savedHabit.getName()).isEqualTo(HABIT_NAME_READ_DAILY);
      assertThat(savedHabit.getDescription()).isEqualTo(HABIT_DESCRIPTION_GENERIC);
    }

    @Test
    void shouldHandleNullDescription_WhenCreatingHabit() {
      CreateHabitRequest requestWithNullDescription =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY, null);
      when(habitRepository.existsByUserAndNameIgnoreCase(testUser, HABIT_NAME_READ_DAILY))
          .thenReturn(false);
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity)).thenReturn(testHabitResponse);

      habitService.createHabit(testUser, requestWithNullDescription);

      ArgumentCaptor<HabitEntity> habitCaptor = ArgumentCaptor.forClass(HabitEntity.class);
      verify(habitRepository).save(habitCaptor.capture());

      HabitEntity savedHabit = habitCaptor.getValue();
      assertThat(savedHabit.getDescription()).isNull();
    }

    @Test
    void shouldThrowException_WhenHabitNameAlreadyExists() {
      when(habitRepository.existsByUserAndNameIgnoreCase(testUser, validRequest.name()))
          .thenReturn(true);

      assertThatThrownBy(() -> habitService.createHabit(testUser, validRequest))
          .isInstanceOf(HabitNameAlreadyExistsException.class);

      verify(habitRepository, never()).save(any(HabitEntity.class));
      verify(habitMapper, never()).toResponse(any(HabitEntity.class));
    }

    @Test
    void shouldCheckNameIgnoreCase_WhenValidatingUniqueness() {
      CreateHabitRequest requestWithDifferentCase =
          new CreateHabitRequest(HABIT_NAME_DIFFERENT_CASE, HABIT_DESCRIPTION_GENERIC);
      when(habitRepository.existsByUserAndNameIgnoreCase(testUser, HABIT_NAME_DIFFERENT_CASE))
          .thenReturn(true);

      assertThatThrownBy(() -> habitService.createHabit(testUser, requestWithDifferentCase))
          .isInstanceOf(HabitNameAlreadyExistsException.class);
    }
  }

  @Nested
  class GetUserHabitsTests {

    @Test
    void shouldReturnUserHabits_WhenHabitsExist() {
      List<HabitEntity> habitEntities = List.of(testHabitEntity);
      List<HabitResponse> expectedResponses = List.of(testHabitResponse);

      when(habitRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(testUser))
          .thenReturn(habitEntities);
      when(habitMapper.toResponse(testHabitEntity)).thenReturn(testHabitResponse);

      List<HabitResponse> result = habitService.getUserHabits(testUser);

      assertThat(result).isEqualTo(expectedResponses);
      verify(habitRepository).findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(testUser);
      verify(habitMapper).toResponse(testHabitEntity);
    }

    @Test
    void shouldReturnEmptyList_WhenNoHabitsExist() {
      when(habitRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(testUser))
          .thenReturn(List.of());

      List<HabitResponse> result = habitService.getUserHabits(testUser);

      assertThat(result).isEmpty();
      verify(habitRepository).findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(testUser);
      verify(habitMapper, never()).toResponse(any(HabitEntity.class));
    }
  }

  @Nested
  class DeleteHabitTests {

    @Test
    void shouldThrowNotFound_WhenHabitNotExist() {
      final UUID TEST_ID = UUID.randomUUID();
      when(habitRepository.findById(TEST_ID))
              .thenReturn(Optional.empty());

      assertThatThrownBy(() -> habitService.delete(TEST_ID, testUser.getId()))
              .isInstanceOf(HabitNotFoundException.class)
              .hasMessage(HABIT_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldThrowAlreadyDeleted_WhenHabitIsDeleted() {
      when(habitRepository.findById(testHabitEntity.getId()))
              .thenReturn(Optional.of(testHabitEntity));
      testHabitEntity.setDeletedAt(Instant.now());

      assertThatThrownBy(() -> habitService.delete(testHabitEntity.getId(), testUser.getId()))
              .isInstanceOf(HabitAlreadyDeletedException.class)
              .hasMessage(HABIT_ALREADY_DELETED_MESSAGE);
    }

    @Test
    void shouldDeleteHabit() {
      when(habitRepository.findById(testHabitEntity.getId()))
              .thenReturn(Optional.of(testHabitEntity));

      habitService.delete(testHabitEntity.getId(), testUser.getId());
      verify(internalHabitService).softDelete(testHabitEntity);
    }
  }
}
