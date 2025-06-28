package com.habittracker.api.habit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {}
