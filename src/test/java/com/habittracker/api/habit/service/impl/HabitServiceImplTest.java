package com.habittracker.api.habit.service.impl;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;
import static com.habittracker.api.habit.constants.HabitTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.mapper.HabitMapper;
import com.habittracker.api.habit.model.Frequency;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.habit.streak.service.StreakService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PagedModel;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

  @Mock private HabitRepository habitRepository;
  @Mock private UserRepository userRepository;
  @Mock private HabitMapper habitMapper;
  @Mock private HabitHelper habitHelper;
  @Mock private StreakService streakService;
  @Mock private DailyCheckInService dailyCheckInService;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private CacheManager cacheManager;
  @InjectMocks private HabitServiceImpl habitService;

  private static final int TEST_CURRENT_STREAK = 20;

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
            Instant.now(),
            false,
            TEST_CURRENT_STREAK,
            EXPECTED_ARCHIVED);
  }

  private void setUpStreakService() {
    when(streakService.calculateStreak(testHabitEntity.getId()))
        .thenReturn(
            new StreakResponse(testHabitEntity.getId(), TEST_CURRENT_STREAK, Instant.now()));
  }

  @Nested
  class CreateHabitTests {

    @Test
    void shouldCreateHabit_WhenValidRequest() {
      setUpStreakService();
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity, TEST_CURRENT_STREAK, false))
          .thenReturn(testHabitResponse);
      when(userRepository.getReferenceById(testUser.getId())).thenReturn(testUser);

      HabitResponse result = habitService.createHabit(testUser.getId(), validRequest);

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
      setUpStreakService();
      CreateHabitRequest requestWithWhitespace =
          new CreateHabitRequest(HABIT_NAME_WHITESPACE, HABIT_DESCRIPTION_WHITESPACE);
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity, TEST_CURRENT_STREAK, false))
          .thenReturn(testHabitResponse);

      habitService.createHabit(testUser.getId(), requestWithWhitespace);

      ArgumentCaptor<HabitEntity> habitCaptor = ArgumentCaptor.forClass(HabitEntity.class);
      verify(habitRepository).save(habitCaptor.capture());

      HabitEntity savedHabit = habitCaptor.getValue();
      assertThat(savedHabit.getName()).isEqualTo(HABIT_NAME_READ_DAILY);
      assertThat(savedHabit.getDescription()).isEqualTo(HABIT_DESCRIPTION_GENERIC);
    }

    @Test
    void shouldHandleNullDescription_WhenCreatingHabit() {
      setUpStreakService();
      CreateHabitRequest requestWithNullDescription =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY, null);
      when(habitRepository.save(any(HabitEntity.class))).thenReturn(testHabitEntity);
      when(habitMapper.toResponse(testHabitEntity, TEST_CURRENT_STREAK, false))
          .thenReturn(testHabitResponse);

      habitService.createHabit(testUser.getId(), requestWithNullDescription);

      ArgumentCaptor<HabitEntity> habitCaptor = ArgumentCaptor.forClass(HabitEntity.class);
      verify(habitRepository).save(habitCaptor.capture());

      HabitEntity savedHabit = habitCaptor.getValue();
      assertThat(savedHabit.getDescription()).isNull();
    }

    @Test
    void shouldThrowException_WhenHabitNameAlreadyExists() {
      doThrow(new HabitNameAlreadyExistsException())
          .when(habitHelper)
          .isUniqueName(testUser.getId(), validRequest.name());
      assertThatThrownBy(() -> habitService.createHabit(testUser.getId(), validRequest))
          .isInstanceOf(HabitNameAlreadyExistsException.class);

      verify(habitRepository, never()).save(any(HabitEntity.class));
      verify(habitMapper, never()).toResponse(any(HabitEntity.class), anyInt(), anyBoolean());
    }

    @Test
    void shouldCheckNameIgnoreCase_WhenValidatingUniqueness() {
      CreateHabitRequest requestWithDifferentCase =
          new CreateHabitRequest(HABIT_NAME_DIFFERENT_CASE, HABIT_DESCRIPTION_GENERIC);
      doThrow(new HabitNameAlreadyExistsException())
          .when(habitHelper)
          .isUniqueName(testUser.getId(), HABIT_NAME_DIFFERENT_CASE);
      assertThatThrownBy(() -> habitService.createHabit(testUser.getId(), requestWithDifferentCase))
          .isInstanceOf(HabitNameAlreadyExistsException.class);
    }
  }

  @Nested
  @SuppressWarnings("unchecked")
  class GetUserHabitsTests {

    @Test
    void shouldReturnUserHabits_WhenHabitsExist() {
      setUpStreakService();
      Page<HabitEntity> habitEntities =
          new PageImpl<>(List.of(testHabitEntity), defaultPageable, 1);
      PagedModel<HabitResponse> expectedResponses =
          new PagedModel<>(new PageImpl<>(List.of(testHabitResponse), defaultPageable, 1));

      when(habitRepository.findAll(any(Specification.class), eq(defaultPageable)))
          .thenReturn(habitEntities);
      when(habitMapper.toResponse(testHabitEntity, TEST_CURRENT_STREAK, false))
          .thenReturn(testHabitResponse);

      PagedModel<HabitResponse> result =
          habitService.getUserHabits(testUser.getId(), defaultPageable, false);

      assertThat(result).isEqualTo(expectedResponses);
      verify(habitRepository).findAll(any(Specification.class), eq(defaultPageable));
      verify(habitMapper).toResponse(testHabitEntity, TEST_CURRENT_STREAK, false);
    }

    @Test
    void shouldReturnEmptyList_WhenNoHabitsExist() {
      when(habitRepository.findAll(any(Specification.class), eq(defaultPageable)))
          .thenReturn(Page.empty());

      PagedModel<HabitResponse> result =
          habitService.getUserHabits(testUser.getId(), defaultPageable, false);

      assertThat(result.getContent()).isEmpty();
      verify(habitRepository).findAll(any(Specification.class), eq(defaultPageable));
      verify(habitMapper, never()).toResponse(any(HabitEntity.class), anyInt(), anyBoolean());
    }
  }

  @Nested
  class DeleteHabitTests {

    @Test
    void shouldThrowNotFound_WhenHabitNotExist() {
      final UUID TEST_ID = UUID.randomUUID();
      when(habitHelper.getNotDeletedOrThrow(TEST_ID)).thenThrow(new HabitNotFoundException());

      assertThatThrownBy(() -> habitService.delete(TEST_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(HABIT_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldDeleteHabitAndEvictCaches() {
      Cache mockCache = mock(Cache.class);
      when(habitHelper.getNotDeletedOrThrow(testHabitEntity.getId())).thenReturn(testHabitEntity);
      when(cacheManager.getCache("userStatistics")).thenReturn(mockCache);
      when(cacheManager.getCache("weeklySummary")).thenReturn(mockCache);

      habitService.delete(testHabitEntity.getId());

      assertThat(testHabitEntity.getDeletedAt()).isNotNull();
      verify(cacheManager).getCache("userStatistics");
      verify(cacheManager).getCache("weeklySummary");
      verify(mockCache, times(2)).evict(testUser.getId());
      verify(redisTemplate).delete(anyString());
    }
  }
}
