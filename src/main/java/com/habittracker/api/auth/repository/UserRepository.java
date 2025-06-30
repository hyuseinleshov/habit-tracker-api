package com.habittracker.api.auth.repository;

import com.habittracker.api.checkin.CheckInEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<CheckInEntity, UUID> {}
