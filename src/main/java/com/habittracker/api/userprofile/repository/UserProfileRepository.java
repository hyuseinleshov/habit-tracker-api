package com.habittracker.api.userprofile.repository;

import com.habittracker.api.userprofile.model.UserProfileEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {}
