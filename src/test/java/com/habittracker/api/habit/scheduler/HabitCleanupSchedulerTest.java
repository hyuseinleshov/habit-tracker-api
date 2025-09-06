package com.habittracker.api.habit.scheduler;

import com.habittracker.api.habit.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HabitCleanupSchedulerTest {

    @Mock
    private HabitRepository habitRepository;

    private HabitCleanupScheduler toTest;

    private static final Period testhabitPeriod = Period.of(0, 0, 5);

    @BeforeEach
    void setUp() {
        this.toTest = new HabitCleanupScheduler(habitRepository, testhabitPeriod);
    }

    @Test
    public void test_CleanUsers_Call_CorrectMethod_With_Valid_Arguments() {
        Instant now = Instant.now();
        toTest.cleanupHabits();
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(habitRepository).deleteAllByDeletedAtBefore(instantArgumentCaptor.capture());
        Instant deleteBefore = instantArgumentCaptor.getValue();
        assertThat(deleteBefore).isBefore(now.minus(testhabitPeriod).plus(3, ChronoUnit.SECONDS));
    }
}