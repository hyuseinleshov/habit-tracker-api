package com.habittracker.api.habit.repository;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.UUID;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID>, JpaSpecificationExecutor<HabitEntity> {

  boolean existsByUserAndNameIgnoreCase(UserEntity user, String name);

  Page<HabitEntity> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(UserEntity user, Pageable pageable);

  long deleteAllByDeletedAtBefore(Instant deleteBefore);


}
