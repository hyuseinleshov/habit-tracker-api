package com.habittracker.api.habit.repository;

import com.habittracker.api.habit.model.HabitEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.habittracker.api.habit.projections.HabitStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HabitRepository
    extends JpaRepository<HabitEntity, UUID>, JpaSpecificationExecutor<HabitEntity> {

  Optional<HabitStatusProjection> findStatusById(UUID id);

  boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

  boolean existsByIdAndUserId(UUID id, UUID userId);

  long deleteAllByDeletedAtBefore(Instant deleteBefore);
}
