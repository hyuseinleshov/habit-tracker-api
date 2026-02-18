package com.habittracker.api.habit.streak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.habit.streak.StreakCalculator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StreakCalculatorTest {

  private StreakCalculator streakCalculator;
  private ZoneId testTimeZone;

  @BeforeEach
  void setUp() {
    streakCalculator = new StreakCalculator();
    testTimeZone = ZoneId.of("America/New_York");
  }

  @Nested
  class CalculateConsecutiveStreakTests {

    @Test
    void shouldReturnZero_WhenCheckInsListIsEmpty() {
      List<CheckInEntity> emptyList = Collections.emptyList();

      int streak = streakCalculator.calculateCurrentStreak(emptyList, testTimeZone);

      assertThat(streak).isZero();
    }

    @Test
    void shouldThrowException_WhenCheckInsIsNull() {
      assertThatThrownBy(() -> streakCalculator.calculateCurrentStreak(null, testTimeZone))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("checkIns cannot be null");
    }

    @Test
    void shouldThrowException_WhenTimeZoneIsNull() {
      List<CheckInEntity> checkIns = List.of(createCheckIn(Instant.now()));

      assertThatThrownBy(() -> streakCalculator.calculateCurrentStreak(checkIns, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("userTimeZone cannot be null");
    }

    @Test
    void shouldReturnOne_WhenSingleCheckInIsToday() {
      Instant today = ZonedDateTime.now(testTimeZone).withHour(14).withMinute(30).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(today));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldReturnOne_WhenSingleCheckInIsYesterday() {
      Instant yesterday =
          ZonedDateTime.now(testTimeZone).minusDays(1).withHour(14).withMinute(0).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(yesterday));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldReturnOne_WhenSingleCheckInIsTwoDaysAgo() {
      Instant twoDaysAgo =
          ZonedDateTime.now(testTimeZone).minusDays(2).withHour(14).withMinute(0).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(twoDaysAgo));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldReturnOne_WhenSingleCheckInIsOneWeekAgo() {
      Instant oneWeekAgo =
          ZonedDateTime.now(testTimeZone).minusDays(7).withHour(10).withMinute(0).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(oneWeekAgo));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldCalculateCorrectStreak_WhenConsecutiveDaysExist() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      List<CheckInEntity> checkIns =
          List.of(
              createCheckIn(now.toInstant()), // Today
              createCheckIn(now.minusDays(1).toInstant()), // Yesterday
              createCheckIn(now.minusDays(2).toInstant()), // 2 days ago
              createCheckIn(now.minusDays(3).toInstant())); // 3 days ago

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(4);
    }

    @Test
    void shouldCalculateCorrectStreak_WhenStreakStartsYesterday() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      List<CheckInEntity> checkIns =
          List.of(
              createCheckIn(now.minusDays(1).toInstant()), // Yesterday
              createCheckIn(now.minusDays(2).toInstant()), // 2 days ago
              createCheckIn(now.minusDays(3).toInstant())); // 3 days ago

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(3);
    }

    @Test
    void shouldStopCounting_WhenGapInDaysIsDetected() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      List<CheckInEntity> checkIns =
          List.of(
              createCheckIn(now.toInstant()), // Today
              createCheckIn(now.minusDays(1).toInstant()), // Yesterday
              createCheckIn(now.minusDays(2).toInstant()), // 2 days ago
              // GAP - missing day 3
              createCheckIn(now.minusDays(4).toInstant()), // 4 days ago (should not count)
              createCheckIn(now.minusDays(5).toInstant())); // 5 days ago (should not count)

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(3);
    }

    @Test
    void shouldCalculateCorrectly_WithLongStreak() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      List<CheckInEntity> checkIns = new ArrayList<>();

      // Create 30-day consecutive streak (one check-in per day)
      for (int i = 0; i < 30; i++) {
        checkIns.add(createCheckIn(now.minusDays(i).withHour(14).toInstant()));
      }

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(30);
    }

    @Test
    void shouldCalculateCorrectly_With100DayStreak() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      List<CheckInEntity> checkIns = new ArrayList<>();

      // Create 100-day consecutive streak
      for (int i = 0; i < 100; i++) {
        checkIns.add(createCheckIn(now.minusDays(i).withHour(10).toInstant()));
      }

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(100);
    }

    @Test
    void shouldHandleCheckInsAtMidnightBoundary() {
      LocalDate today = LocalDate.now(testTimeZone);

      // Check-in at 23:59 yesterday
      Instant yesterdayBeforeMidnight =
          today.minusDays(1).atTime(23, 59).atZone(testTimeZone).toInstant();
      // Check-in at 00:01 today
      Instant todayAfterMidnight = today.atTime(0, 1).atZone(testTimeZone).toInstant();

      List<CheckInEntity> checkIns =
          List.of(createCheckIn(todayAfterMidnight), createCheckIn(yesterdayBeforeMidnight));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(2);
    }

    @Test
    void shouldRespectUserTimeZone_Tokyo() {
      ZoneId tokyoTz = ZoneId.of("Asia/Tokyo");
      ZonedDateTime nowTokyo = ZonedDateTime.now(tokyoTz);

      List<CheckInEntity> checkIns =
          List.of(
              createCheckIn(nowTokyo.toInstant()),
              createCheckIn(nowTokyo.minusDays(1).toInstant()),
              createCheckIn(nowTokyo.minusDays(2).toInstant()));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, tokyoTz);

      assertThat(streak).isEqualTo(3);
    }

    @Test
    void shouldRespectUserTimeZone_London() {
      ZoneId londonTz = ZoneId.of("Europe/London");
      ZonedDateTime nowLondon = ZonedDateTime.now(londonTz);

      List<CheckInEntity> checkIns =
          List.of(
              createCheckIn(nowLondon.toInstant()),
              createCheckIn(nowLondon.minusDays(1).toInstant()));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, londonTz);

      assertThat(streak).isEqualTo(2);
    }

    @Test
    void shouldReturnOne_WhenOnlyCheckInIsFromLastYear() {
      ZonedDateTime now = ZonedDateTime.now(testTimeZone);
      Instant lastYear = now.minusYears(1).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(lastYear));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldReturnOne_WhenCheckInIsAtStartOfToday() {
      LocalDate today = LocalDate.now(testTimeZone);
      Instant startOfToday = today.atStartOfDay(testTimeZone).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(startOfToday));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }

    @Test
    void shouldReturnOne_WhenCheckInIsAtEndOfToday() {
      LocalDate today = LocalDate.now(testTimeZone);
      Instant endOfToday = today.atTime(23, 59, 59).atZone(testTimeZone).toInstant();
      List<CheckInEntity> checkIns = List.of(createCheckIn(endOfToday));

      int streak = streakCalculator.calculateCurrentStreak(checkIns, testTimeZone);

      assertThat(streak).isEqualTo(1);
    }
  }

  private CheckInEntity createCheckIn(Instant createdAt) {
    CheckInEntity checkIn = new CheckInEntity();
    checkIn.setCreatedAt(createdAt);
    return checkIn;
  }
}
