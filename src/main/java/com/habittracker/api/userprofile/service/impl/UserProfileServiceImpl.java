package com.habittracker.api.userprofile.service.impl;

import static com.habittracker.api.userprofile.constants.UserProfileConstants.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.utils.TimezoneUtils;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.mapper.UserProfileMapper;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import com.habittracker.api.userprofile.service.UserProfileService;
import java.util.UUID;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserProfileMapper userProfileMapper;
  private final Validator validator;

  @Override
  @Transactional
  public void createProfile(UserEntity user, String timezone) {
    if (user == null) throw new IllegalArgumentException(USER_CANT_BE_NULL_MESSAGE);
    if (!TimezoneUtils.isValidTimezone(timezone))
      throw new IllegalArgumentException(INVALID_TIMEZONE_MESSAGE);
    UserProfileEntity userProfile = new UserProfileEntity();
    userProfile.setUser(user);
    userProfile.setTimezone(timezone);
    userProfileRepository.save(userProfile);
  }

  @Override
  @Cacheable(value = "userProfiles", key = "#id")
  @Transactional(readOnly = true)
  public UserProfileDTO getUserProfileById(UUID id) {
    return userProfileMapper.toUserProfileDTO(byId(id));
  }

  private UserProfileEntity byId(UUID id) {
    return userProfileRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException(USER_PROFILE_NOT_FOUND_MESSAGE));
  }

  @Override
  @Transactional
  @CacheEvict(value = "userProfiles", key = "#id")
  public UserProfileDTO updateUserProfile(UUID id, UserProfileDTO userProfileDTO) {
    if(!validator.validate(userProfileDTO).isEmpty()) {
      throw new IllegalArgumentException(USER_PROFILE_DATA_NOT_VALID_MESSAGE);
    }
    UserProfileEntity profile = byId(id);
    BeanUtils.copyProperties(userProfileDTO, profile);
    UserProfileEntity updated = userProfileRepository.save(profile);
    return userProfileMapper.toUserProfileDTO(updated);
  }
}
