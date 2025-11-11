package com.habittracker.api.checkin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.utils.AuthUtils;
import com.habittracker.api.checkin.dto.StreakResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.repository.CheckInRepository;
import com.habittracker.api.habit.helpers.HabitHelper;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.streak.StreakCalculator;
import com.habittracker.api.habit.streak.service.impl.StreakServiceImpl;
import com.habittracker.api.user.model.UserProfileEntity;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class StreakServiceImplTest {

  @Mock private CheckInRepository checkInRepository;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private StreakCalculator streakCalculator;
  @Mock private ValueOperations<String, Object> valueOperations;
  @Mock private HabitHelper habitHelper;

  @InjectMocks private StreakServiceImpl streakService;

  private UUID testHabitId;
  private HabitEntity testHabit;
  private UserProfileEntity testUserProfile;
  private ZoneId testTimeZone;
  private static UUID testUserId;
  private static MockedStatic<AuthUtils> utilsMock;

  @BeforeAll
  static void beforeAll() {
    testUserId = UUID.randomUUID();
    utilsMock = mockStatic(AuthUtils.class);
    utilsMock.when(AuthUtils::getUserId).thenReturn(testUserId);
    utilsMock.when(AuthUtils::getUserTimeZone).thenReturn(ZoneId.systemDefault());
  }

  @AfterAll
  static void afterAll() {
    utilsMock.close();
  }

  @BeforeEach
  void setUp() {
    testHabitId = UUID.randomUUID();
    testTimeZone = ZoneId.of("America/New_York");

    UserEntity testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());

    testUserProfile = new UserProfileEntity();
    testUserProfile.setTimeZone("America/New_York");
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
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId))
          .thenReturn(cachedStreak);

      StreakResponse response = streakService.calculateStreak(testHabitId);
      assertThat(response.habitId()).isEqualTo(testHabitId);
      assertThat(response.currentStreak()).isEqualTo(5);
      assertThat(response.calculatedAt()).isNotNull();

      // Should not hit database when cache exists
      verify(checkInRepository, never()).findFirstByHabitIdOrderByCreatedAtDesc(any());
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateCurrentStreak(any(), any());
    }

    @Test
    void shouldReturnZero_WhenNoCheckInsExist() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      StreakResponse response = streakService.calculateStreak(testHabitId);
      assertThat(response.currentStreak()).isZero();

      // Should not fetch all check-ins if most recent doesn't exist
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateCurrentStreak(any(), any());
    }

    @Test
    void shouldReturnZero_WhenMostRecentCheckInIsTooOld() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity oldCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(5).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(oldCheckIn));

      StreakResponse response = streakService.calculateStreak(testHabitId);
      assertThat(response.currentStreak()).isZero();

      // Should not fetch all check-ins if streak is broken
      verify(checkInRepository, never()).findByHabitIdOrderByCreatedAtDesc(any());
      verify(streakCalculator, never()).calculateCurrentStreak(any(), any());
    }

    @Test
    void shouldCacheCalculatedStreak_WhenCacheMiss() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

      verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture());

      assertThat(keyCaptor.getValue()).isEqualTo("streak:" + testUserId + ":" + testHabitId);
      assertThat(valueCaptor.getValue()).isEqualTo(1);

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());
      assertThat(expireAtCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldFetchAllCheckIns_OnlyWhenStreakIsActive() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

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
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId))
          .thenReturn(currentStreak);

      streakService.incrementStreak(testHabit);

      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
      verify(valueOperations)
          .set(eq("streak:" + testUserId + ":" + testHabitId), valueCaptor.capture());

      assertThat(valueCaptor.getValue()).isEqualTo(6);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), any(Instant.class));
    }

    @Test
    void shouldIncrementFromOne_WhenNoExistingStreak() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      streakService.incrementStreak(testHabit);

      ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
      // Only caches once when incrementing (0 is not cached, but 1 is)
      verify(valueOperations, times(1))
          .set(eq("streak:" + testUserId + ":" + testHabitId), valueCaptor.capture());

      assertThat(valueCaptor.getValue()).isEqualTo(1);
      verify(redisTemplate, times(1))
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), any(Instant.class));
    }

    @Test
    void shouldCalculateThenIncrement_WhenCacheMiss() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

      streakService.incrementStreak(testHabit);

      // Should cache calculated streak (1), then increment and cache again (2)
      verify(valueOperations, times(2)).set(eq("streak:" + testUserId + ":" + testHabitId), any());
      verify(redisTemplate, times(2))
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), any(Instant.class));
    }

    @Test
    void shouldUpdateCacheWithCorrectTTL() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(3);

      streakService.incrementStreak(testHabit);

      verify(valueOperations).set(eq("streak:" + testUserId + ":" + testHabitId), eq(4));

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());

      // Expiration should be set to midnight tomorrow (in the future)
      assertThat(expireAtCaptor.getValue()).isAfter(Instant.now());
    }
  }

  @Nested
  class CacheTTLTests {

    @Test
    void shouldSetCacheTTLToOneDayFromNow_WhenLastCheckInWasYesterday() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      // Last check-in was yesterday
      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(testTimeZone).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

      Instant beforeCall = Instant.now();
      streakService.calculateStreak(testHabitId);

      Instant afterCall = Instant.now();

      verify(valueOperations).set(eq("streak:" + testUserId + ":" + testHabitId), any());

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());

      // Expiration should be at midnight tonight (less than 24 hours from now)
      long hoursUntilExpiry =
          (expireAtCaptor.getValue().toEpochMilli() - afterCall.toEpochMilli()) / (1000 * 60 * 60);
      assertThat(hoursUntilExpiry).isLessThan(24);
      assertThat(expireAtCaptor.getValue()).isAfter(beforeCall);
    }

    @Test
    void shouldSetCacheTTLToTwoDaysFromNow_WhenLastCheckInWasToday() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      // Last check-in was today
      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(testTimeZone).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

      Instant beforeCall = Instant.now();

      streakService.calculateStreak(testHabitId);

      verify(valueOperations).set(eq("streak:" + testUserId + ":" + testHabitId), any());

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());

      // Expiration should be at midnight tomorrow (between 24-48 hours from now)
      long hoursUntilExpiry =
          (expireAtCaptor.getValue().toEpochMilli() - beforeCall.toEpochMilli()) / (1000 * 60 * 60);
      assertThat(hoursUntilExpiry).isGreaterThanOrEqualTo(24);
      assertThat(hoursUntilExpiry).isLessThan(48);
    }

    @Test
    void shouldNotCache_WhenNoCheckInsExist() {
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      // No check-ins
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.empty());

      utilsMock.when(AuthUtils::getUserTimeZone).thenReturn(ZoneId.systemDefault());
      StreakResponse response = streakService.calculateStreak(testHabitId);
      assertThat(response.currentStreak()).isZero();

      // Should not cache when there are no check-ins
      verify(valueOperations, never()).set(any(), any());
      verify(redisTemplate, never()).expireAt(any(), any(Instant.class));
    }

    @Test
    void shouldUseTodaysTTL_WhenIncrementingStreakAfterTodaysCheckIn() {
      // Simulate having a cached streak and incrementing after today's check-in
      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(5);

      Instant beforeCall = Instant.now();
      // After optimization, incrementStreak uses today's date directly (no DB query)

      streakService.incrementStreak(testHabit);

      verify(valueOperations).set(eq("streak:" + testUserId + ":" + testHabitId), eq(6));

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());

      // Since check-in is today, expiration should be at midnight tomorrow (24-48 hours)
      long hoursUntilExpiry =
          (expireAtCaptor.getValue().toEpochMilli() - beforeCall.toEpochMilli()) / (1000 * 60 * 60);
      assertThat(hoursUntilExpiry).isGreaterThanOrEqualTo(24);
      assertThat(hoursUntilExpiry).isLessThan(48);
    }

    @Test
    void shouldSetCorrectTTL_ForDifferentTimezones_Yesterday() {
      testUserProfile.setTimeZone("Asia/Tokyo");
      ZoneId tokyoTz = ZoneId.of("Asia/Tokyo");

      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      // Last check-in was yesterday in Tokyo timeZone
      CheckInEntity yesterdayCheckIn =
          createCheckIn(ZonedDateTime.now(tokyoTz).minusDays(1).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(yesterdayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(yesterdayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), any())).thenReturn(1);

      Instant beforeCall = Instant.now();

      streakService.calculateStreak(testHabitId);

      verify(valueOperations).set(eq("streak:" + testUserId + ":" + testHabitId), any());

      ArgumentCaptor<Instant> expireAtCaptor = ArgumentCaptor.forClass(Instant.class);
      verify(redisTemplate)
          .expireAt(eq("streak:" + testUserId + ":" + testHabitId), expireAtCaptor.capture());

      // Expiration should be at Tokyo midnight tonight (less than 24 hours, but could be very soon)
      Instant expireAt = expireAtCaptor.getValue();
      assertThat(expireAt).isAfter(beforeCall);

      long millisUntilExpiry = expireAt.toEpochMilli() - beforeCall.toEpochMilli();
      long hoursUntilExpiry = millisUntilExpiry / (1000 * 60 * 60);
      assertThat(hoursUntilExpiry).isLessThan(24);
    }
  }

  @Nested
  class TimezoneTests {

    @Test
    void shouldRespectTokyoTimezone() {
      testUserProfile.setTimeZone("Asia/Tokyo");
      ZoneId tokyoTz = ZoneId.of("Asia/Tokyo");
      utilsMock.when(AuthUtils::getUserTimeZone).thenReturn(tokyoTz);

      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(tokyoTz).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), eq(tokyoTz))).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      verify(streakCalculator).calculateCurrentStreak(any(), eq(tokyoTz));
    }

    @Test
    void shouldRespectLondonTimezone() {
      testUserProfile.setTimeZone("Europe/London");
      ZoneId londonTz = ZoneId.of("Europe/London");
      utilsMock.when(AuthUtils::getUserTimeZone).thenReturn(londonTz);

      when(valueOperations.get("streak:" + testUserId + ":" + testHabitId)).thenReturn(null);

      CheckInEntity todayCheckIn = createCheckIn(ZonedDateTime.now(londonTz).toInstant());
      when(checkInRepository.findFirstByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(Optional.of(todayCheckIn));
      when(checkInRepository.findByHabitIdOrderByCreatedAtDesc(testHabitId))
          .thenReturn(List.of(todayCheckIn));
      when(streakCalculator.calculateCurrentStreak(any(), eq(londonTz))).thenReturn(1);

      streakService.calculateStreak(testHabitId);

      verify(streakCalculator).calculateCurrentStreak(any(), eq(londonTz));
    }
  }

  private CheckInEntity createCheckIn(Instant createdAt) {
    CheckInEntity checkIn = new CheckInEntity();
    checkIn.setCreatedAt(createdAt);
    return checkIn;
  }
}
