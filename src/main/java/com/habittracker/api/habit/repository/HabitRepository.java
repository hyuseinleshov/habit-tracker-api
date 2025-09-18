package com.habittracker.api.habit.repository;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HabitRepository
    extends JpaRepository<HabitEntity, UUID>, JpaSpecificationExecutor<HabitEntity> {

  boolean existsByUserAndNameIgnoreCase(UserEntity user, String name);

  boolean existsByIdAndUserId(UUID id, UUID userId);

  long deleteAllByDeletedAtBefore(Instant deleteBefore);
}
