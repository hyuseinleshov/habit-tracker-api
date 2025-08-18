package com.habittracker.api.userprofile.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import java.util.UUID;

public interface UserProfileService {

  void createProfile(UserEntity user, String timezone);

  UserProfileDTO getById(UUID id);

  UserProfileDTO update(UUID id, UserProfileDTO userProfileDTO);

  void delete(UUID uuid);
}
