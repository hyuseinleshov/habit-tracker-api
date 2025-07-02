package com.habittracker.api.checkin.repository;

import com.habittracker.api.checkin.model.CheckInEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckInEntity, UUID> {}
