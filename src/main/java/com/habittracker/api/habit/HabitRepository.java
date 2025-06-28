package com.habittracker.api.habit;

import com.habittracker.api.checkin.CheckInEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {
}
