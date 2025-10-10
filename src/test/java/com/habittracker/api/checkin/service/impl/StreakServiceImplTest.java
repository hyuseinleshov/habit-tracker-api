package com.habittracker.api.checkin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.checkin.service.StreakCalculator;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.user.model.UserProfileEntity;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class StreakServiceImplTest {

  @Mock private CheckInRepository checkInRepository;
  @Mock private HabitHelper habitHelper;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private StreakCalculator streakCalculator;
  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private StreakServiceImpl streakService;

  private UUID testHabitId;
  private HabitEntity testHabit;
  private UserEntity testUser;
  private UserProfileEntity testUserProfile;
  private ZoneId testTimeZone;

  @BeforeEach
  void setUp() {
    testHabitId = UUID.randomUUID();
    testTimeZone = ZoneId.of("America/New_York");

    testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());

    testUserProfile = new UserProfileEntity();
    testUserProfile.setTimezone("America/New_York");
    testUser.setUserProfile(testUserProfile);

    testHabit = new HabitEntity();
    testHabit.setId(testHabitId);
    testHabit.setUser(testUser);
    testHabit.setName("Test Habit");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Nested
  class CalculateStreakTests {

    @Test
    void shouldReturnCachedStreak_WhenCacheHit() {
      int cachedStreak = 5;
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(cachedStreak);

      StreakResponse response = streakService.calculateStreak(testHabitId);

      assertThat(response.habitId()).isEqualTo(testHabitId);
      assertThat(response.currentStreak()).isEqualTo(5);
      assertThat(response.calculatedAt()).isNotNull();

      // Should not hit database when cache exists
      verify(checkInRepository, never()).findFirstByHabitIdOrderByCreatedAtDesc(any());
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateConsecutiveStreak(any(), any());
    }

    @Test
    void shouldCalculateFromDatabase_WhenCacheMiss() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(1).toInstant());

      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn, yesterdayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), eq(testTimeZone))).thenReturn(2);

      StreakResponse response = streakService.calculateStreak(testHabitId);

      assertThat(response.habitId()).isEqualTo(testHabitId);
      assertThat(response.currentStreak()).isEqualTo(2);

      verify(checkInRepository).findFirstByHabitIdOrderByCreatedAtDesc(testHabitId);
      verify(checkInRepository).findByHabitIdOrderByCreatedAtDesc(testHabitId);
      verify(streakCalculator).calculateConsecutiveStreak(any(), eq(testTimeZone));
    }

    @Test
    void shouldReturnZero_WhenNoCheckInsExist() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      StreakResponse response = streakService.calculateStreak(testHabitId);

      assertThat(response.currentStreak()).isZero();

      // Should not fetch all check-ins if most recent doesn't exist
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateConsecutiveStreak(any(), any());
    }

    @Test
    void shouldReturnZero_WhenMostRecentCheckInIsTooOld() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity oldCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(5).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(oldCheckIn));

      StreakResponse response = streakService.calculateStreak(testHabitId);

      assertThat(response.currentStreak()).isZero();

      // Should not fetch all check-ins if streak is broken
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateConsecutiveStreak(any(), any());
    }

    @Test
    void shouldCacheCalculatedStreak_WhenCacheMiss() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

      verify(valueOperations)
          .set(keyCaptor.capture(), valueCaptor.capture(), durationCaptor.capture());

      assertThat(keyCaptor.getValue()).isEqualTo("streak:" + testHabitId);
      assertThat(valueCaptor.getValue()).isEqualTo(1);
      assertThat(durationCaptor.getValue()).isNotNull();
      assertThat(durationCaptor.getValue().toHours()).isGreaterThan(24); // At least a day
    }

    @Test
    void shouldFetchAllCheckIns_OnlyWhenStreakIsActive() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      // Verify optimization: first query for most recent, then all if active
      verify(checkInRepository).findFirstByHabitIdOrderByCreatedAtDesc(testHabitId);
      verify(checkInRepository).findByHabitIdOrderByCreatedAtDesc(testHabitId);
    }
  }

  @Nested
  class IncrementStreakTests {

    @Test
    void shouldIncrementStreak_WhenCacheExists() {
      int currentStreak = 5;
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(currentStreak);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      streakService.incrementStreak(testHabitId);

      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
      verify(valueOperations)
          .set(eq("streak:" + testHabitId), valueCaptor.capture(), any(Duration.class));

      assertThat(valueCaptor.getValue()).isEqualTo(6);
    }

    @Test
    void shouldIncrementFromOne_WhenNoExistingStreak() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      streakService.incrementStreak(testHabitId);

      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
      verify(valueOperations, times(2))
          .set(eq("streak:" + testHabitId), valueCaptor.capture(), any(Duration.class));

      // First cache: 0, then increment to 1
      assertThat(valueCaptor.getValue()).isEqualTo(1);
    }

    @Test
    void shouldCalculateThenIncrement_WhenCacheMiss() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.incrementStreak(testHabitId);

      // Should cache calculated streak (1), then increment and cache again (2)
      verify(valueOperations, times(2))
          .set(eq("streak:" + testHabitId), any(), any(Duration.class));
    }

    @Test
    void shouldUpdateCacheWithCorrectTTL() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(3);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      streakService.incrementStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), eq(4), durationCaptor.capture());

      // TTL should be until midnight of day after tomorrow (at least 24 hours)
      assertThat(durationCaptor.getValue().toHours()).isGreaterThanOrEqualTo(24);
      assertThat(durationCaptor.getValue().toHours()).isLessThanOrEqualTo(72);
    }
  }

  @Nested
  class CacheTTLTests {

    @Test
    void shouldSetCacheTTLToOneDayFromNow_WhenLastCheckInWasYesterday() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      // Last check-in was yesterday
      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), any(), durationCaptor.capture());

      // TTL should be until midnight tonight (1 day from now)
      // Should be less than 24 hours (since we're partway through today)
      assertThat(durationCaptor.getValue().toHours()).isLessThan(24);
      assertThat(durationCaptor.getValue().toHours()).isGreaterThan(0);
    }

    @Test
    void shouldSetCacheTTLToTwoDaysFromNow_WhenLastCheckInWasToday() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      // Last check-in was today
      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), any(), durationCaptor.capture());

      // TTL should be until midnight tomorrow (2 days from now)
      // Should be at least 24 hours, but less than 48 hours
      assertThat(durationCaptor.getValue().toHours()).isGreaterThanOrEqualTo(24);
      assertThat(durationCaptor.getValue().toHours()).isLessThan(48);
    }

    @Test
    void shouldSetCacheTTLToTwoDays_WhenNoCheckInsExist() {
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      // No check-ins
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), any(), durationCaptor.capture());

      // Default TTL should be 2 days (until midnight tomorrow)
      assertThat(durationCaptor.getValue().toHours()).isGreaterThanOrEqualTo(24);
      assertThat(durationCaptor.getValue().toHours()).isLessThan(48);
    }

    @Test
    void shouldUseTodaysTTL_WhenIncrementingStreakAfterTodaysCheckIn() {
      // Simulate having a cached streak and incrementing after today's check-in
      when(valueOperations.get("streak:" + testHabitId)).thenReturn(5);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      // After optimization, incrementStreak uses today's date directly (no DB query)
      streakService.incrementStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), eq(6), durationCaptor.capture());

      // Since check-in is today, TTL should be ~48 hours (until tomorrow midnight)
      assertThat(durationCaptor.getValue().toHours()).isGreaterThanOrEqualTo(24);
      assertThat(durationCaptor.getValue().toHours()).isLessThan(48);
    }

    @Test
    void shouldSetCorrectTTL_ForDifferentTimezones_Yesterday() {
      testUserProfile.setTimezone("Asia/Tokyo");
      ZoneId tokyoTz = ZoneId.of("Asia/Tokyo");

      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      // Last check-in was yesterday in Tokyo timezone
      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(tokyoTz).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
      verify(valueOperations).set(eq("streak:" + testHabitId), any(), durationCaptor.capture());

      // TTL should be until Tokyo midnight tonight (less than 24 hours)
      assertThat(durationCaptor.getValue().toHours()).isLessThan(24);
      assertThat(durationCaptor.getValue().toHours()).isGreaterThan(0);
    }
  }

  @Nested
  class TimezoneTests {

    @Test
    void shouldRespectTokyoTimezone() {
      testUserProfile.setTimezone("Asia/Tokyo");
      ZoneId tokyoTz = ZoneId.of("Asia/Tokyo");

      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(tokyoTz).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), eq(tokyoTz))).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      verify(streakCalculator).calculateConsecutiveStreak(any(), eq(tokyoTz));
    }

    @Test
    void shouldRespectLondonTimezone() {
      testUserProfile.setTimezone("Europe/London");
      ZoneId londonTz = ZoneId.of("Europe/London");

      when(valueOperations.get("streak:" + testHabitId)).thenReturn(null);
      when(habitHelper.getNotDeletedOrThrow(testHabitId)).thenReturn(testHabit);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(londonTz).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateConsecutiveStreak(any(), eq(londonTz))).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      verify(streakCalculator).calculateConsecutiveStreak(any(), eq(londonTz));
    }
  }

  private CheckInEntity createCheckIn(Instant createdAt) {
    CheckInEntity checkIn = new CheckInEntity();
    checkIn.setCreatedAt(createdAt);
    return checkIn;
  }
}
