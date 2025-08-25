package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.model.UserProfileEntity;

public interface UserProfileService {

  UserProfileEntity createProfile(UserEntity user, String timezone);

  UserProfileDTO toProfileDTO(UserProfileEntity profileEntity);

  UserProfileDTO update(UserProfileEntity profile, UserProfileDTO userProfileDTO);
}
