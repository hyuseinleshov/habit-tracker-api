package com.habittracker.api.user;

import com.habittracker.api.checkin.CheckInEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<CheckInEntity, UUID> {
}
