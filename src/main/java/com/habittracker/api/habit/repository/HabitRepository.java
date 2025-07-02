package com.habittracker.api.habit.repository;

import com.habittracker.api.habit.model.HabitEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {}
