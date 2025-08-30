package com.habittracker.api.habit.repository;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {

    boolean existsByUserAndNameIgnoreCase(UserEntity user, String name);

    List<HabitEntity> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(UserEntity user);
}
