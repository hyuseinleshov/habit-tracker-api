package com.habittracker.api.habit.repository;

import java.util.UUID;

import com.habittracker.api.habit.model.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {}
