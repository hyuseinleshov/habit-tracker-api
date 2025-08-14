package com.habittracker.api.userprofile.repository;

import com.habittracker.api.userprofile.model.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
}
