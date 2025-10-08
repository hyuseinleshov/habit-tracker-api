package com.habittracker.api.checkin.repository;

import com.habittracker.api.checkin.model.CheckInEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckInRepository
    extends JpaRepository<CheckInEntity, UUID>, JpaSpecificationExecutor<CheckInEntity> {

  boolean existsByIdAndHabitUserId(UUID id, UUID userId);

  List<CheckInEntity> findByHabitIdOrderByCreatedAtDesc(UUID habitId);
}
