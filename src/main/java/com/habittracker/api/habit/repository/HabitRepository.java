package com.habittracker.api.habit.repository;

import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.projections.HabitStatusProjection;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabitRepository
    extends JpaRepository<HabitEntity, UUID>, JpaSpecificationExecutor<HabitEntity> {

  Optional<HabitStatusProjection> findStatusById(UUID id);

  @Query(
      "SELECT h from HabitEntity h WHERE h.user.id = :userId "
          + "AND h.deletedAt IS NULL ORDER BY h.bestStreak DESC "
          + "LIMIT 1")
  Optional<HabitEntity> findBestStreakByUserId(@Param("userId") UUID userId);

  @Query(
      "SELECT COUNT(DISTINCT h.id) FROM HabitEntity h "
          + "WHERE h.user.id = :userId AND h.deletedAt IS NULL AND "
          + "EXISTS (SELECT 1 FROM CheckInEntity c WHERE c.habit = h AND c.createdAt >= :since)")
  long countHabitsWithRecentCheckIns(@Param("userId") UUID userId, @Param("since") Instant since);

  boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndUserId(String name, UUID userId);

  boolean existsByIdAndUserId(UUID id, UUID userId);

  long deleteAllByDeletedAtBefore(Instant deleteBefore);

  long countByUserIdAndDeletedAtIsNull(UUID id);
}
