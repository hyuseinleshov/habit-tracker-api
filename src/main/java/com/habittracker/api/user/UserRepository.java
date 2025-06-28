package com.habittracker.api.user;

import com.habittracker.api.checkin.CheckInEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<CheckInEntity, UUID> {}
