package com.habittracker.api.checkin.repository;

import com.habittracker.api.checkin.model.CheckInEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckInRepository
    extends JpaRepository<CheckInEntity, UUID>, JpaSpecificationExecutor<CheckInEntity> {

  boolean existsByIdAndHabitUserId(UUID id, UUID userId);

  List<CheckInEntity> findByHabitIdOrderByCreatedAtDesc(UUID habitId);

  long countByHabitId(UUID habitId);

  long countByHabitUserId(UUID userId);

  Optional<CheckInEntity> findFirstByHabitIdOrderByCreatedAtDesc(UUID habitId);

  Optional<CheckInEntity> findFirstByHabitUserIdOrderByCreatedAtDesc(UUID userId);

  Set<CheckInEntity> findByHabitUserIdAndCreatedAtBetween(
      UUID userId, Instant createdAtAfter, Instant createdAtBefore);
}
