package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.model.UserProfileEntity;
import java.util.UUID;

public interface UserProfileService {

  UserProfileEntity createProfile(UserEntity user, String timezone);

  UserProfileDTO toProfileDTO(UUID userId);

  UserProfileDTO update(UUID userId, UserProfileDTO userProfileDTO);
}
