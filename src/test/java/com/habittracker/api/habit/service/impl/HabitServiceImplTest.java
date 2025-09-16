package com.habittracker.api.habit.service.impl;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;
import static com.habittracker.api.habit.constants.HabitTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.Frequency;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

  @Mock private HabitRepository habitRepository;
  @Mock private HabitMapper habitMapper;
  @InjectMocks private HabitServiceImpl habitService;

  private UserEntity testUser;
  private CreateHabitRequest validRequest;
  private HabitEntity testHabitEntity;
  private HabitResponse testHabitResponse;
  private Pageable defaultPageable;

  @BeforeEach
  void setUp() {
    testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());
    validRequest = new CreateHabitRequest(HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_LONG);
    defaultPageable = Pageable.ofSize(10).withPage(0);

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
  @SuppressWarnings("unchecked")
  class GetUserHabitsTests {

    @Test
    void shouldReturnUserHabits_WhenHabitsExist() {
      Page<HabitEntity> habitEntities =
          new PageImpl<>(List.of(testHabitEntity), defaultPageable, 1);
      PagedModel<HabitResponse> expectedResponses =
          new PagedModel<>(new PageImpl<>(List.of(testHabitResponse), defaultPageable, 1));

      when(habitRepository.findAll(any(Specification.class), defaultPageable))
          .thenReturn(habitEntities);
      when(habitMapper.toResponse(testHabitEntity)).thenReturn(testHabitResponse);

      PagedModel<HabitResponse> result =
          habitService.getUserHabits(testUser, defaultPageable, false);

      assertThat(result).isEqualTo(expectedResponses);
      verify(habitRepository).findAll(any(Specification.class), defaultPageable);
      verify(habitMapper).toResponse(testHabitEntity);
    }

    @Test
    void shouldReturnEmptyList_WhenNoHabitsExist() {
      when(habitRepository.findAll(any(Specification.class), defaultPageable))
          .thenReturn(Page.empty());

      PagedModel<HabitResponse> result =
          habitService.getUserHabits(testUser, defaultPageable, false);

      assertThat(result.getContent()).isEmpty();
      verify(habitRepository).findAll(any(Specification.class), defaultPageable);
      verify(habitMapper, never()).toResponse(any(HabitEntity.class));
    }
  }

  @Nested
  class DeleteHabitTests {

    @Test
    void shouldThrowNotFound_WhenHabitNotExist() {
      final UUID TEST_ID = UUID.randomUUID();
      when(habitRepository.findById(TEST_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> habitService.delete(TEST_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(HABIT_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldDeleteHabit() {
      when(habitRepository.findById(testHabitEntity.getId()))
          .thenReturn(Optional.of(testHabitEntity));

      habitService.delete(testHabitEntity.getId());
    }
  }
}
