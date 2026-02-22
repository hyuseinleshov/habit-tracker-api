package com.habittracker.api.checkin.repository;

import com.habittracker.api.checkin.model.CheckInEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckInRepository
    extends JpaRepository<CheckInEntity, UUID>, JpaSpecificationExecutor<CheckInEntity> {

  boolean existsByIdAndHabitUserId(UUID id, UUID userId);

  List<CheckInEntity> findByHabitIdOrderByCreatedAtDesc(UUID habitId);

  long countByHabitId(UUID habitId);

  @Query(
      "SELECT COUNT(c) FROM CheckInEntity c WHERE c.habit.user.id = :userId"
          + " AND c.habit.deletedAt IS NULL")
  long countByHabitUserId(@Param("userId") UUID userId);

  Optional<CheckInEntity> findFirstByHabitIdOrderByCreatedAtDesc(UUID habitId);

  @Query(
      "SELECT c FROM CheckInEntity c WHERE c.habit.user.id = :userId"
          + " AND c.habit.deletedAt IS NULL ORDER BY c.createdAt DESC LIMIT 1")
  Optional<CheckInEntity> findFirstByHabitUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

  @Query(
      "SELECT c FROM CheckInEntity c WHERE c.habit.user.id = :userId"
          + " AND c.habit.deletedAt IS NULL"
          + " AND c.createdAt BETWEEN :createdAtAfter AND :createdAtBefore")
  Set<CheckInEntity> findByHabitUserIdAndCreatedAtBetween(
      @Param("userId") UUID userId,
      @Param("createdAtAfter") Instant createdAtAfter,
      @Param("createdAtBefore") Instant createdAtBefore);
}
