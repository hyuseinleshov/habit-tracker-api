package com.habittracker.api.userprofile.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.mapper.UserProfileMapper;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import com.habittracker.api.userprofile.service.UserProfileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserProfileMapper userProfileMapper;

  @Override
  public void createProfile(UserEntity user, String timezone) {
    UserProfileEntity userProfile = new UserProfileEntity();
    userProfile.setUser(user);
    userProfile.setTimezone(timezone);
    userProfileRepository.save(userProfile);
  }

  @Override
  @Cacheable(value = "userProfiles", key = "#id")
  public UserProfileDTO getUserProfileById(UUID id) {
    return userProfileMapper.toUserProfileDTO(userProfileRepository.findById(id).orElse(null));
  }
}
