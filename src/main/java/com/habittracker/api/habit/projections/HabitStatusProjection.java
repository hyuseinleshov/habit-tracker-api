package com.habittracker.api.habit.projections;

import java.time.Instant;
import java.util.UUID;

public interface HabitStatusProjection {
    UUID getId();
    Instant getDeletedAt();
}
