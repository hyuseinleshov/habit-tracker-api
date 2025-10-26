package com.habittracker.api.habit.repository;

import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.projections.HabitStatusProjection;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface HabitRepository
    extends JpaRepository<HabitEntity, UUID>, JpaSpecificationExecutor<HabitEntity> {

  Optional<HabitStatusProjection> findStatusById(UUID id);

  @Query(
      "SELECT h from HabitEntity h WHERE h.user.id = :userId "
          + "AND h.deletedAt IS NULL ORDER BY h.bestStreak DESC "
          + "LIMIT 1")
  Optional<HabitEntity> findBestStreakByUserId(@Param("userId") UUID userId);

  boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

  boolean existsByIdAndUserId(UUID id, UUID userId);

  long deleteAllByDeletedAtBefore(Instant deleteBefore);
}
