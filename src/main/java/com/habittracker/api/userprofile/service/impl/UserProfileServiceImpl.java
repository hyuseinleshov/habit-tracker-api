package com.habittracker.api.userprofile.service.impl;

import static com.habittracker.api.userprofile.constants.UserProfileConstants.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.utils.TimezoneUtils;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.exception.UserProfileNotFoundException;
import com.habittracker.api.userprofile.mapper.UserProfileMapper;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import com.habittracker.api.userprofile.service.UserProfileService;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.UUID;
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
  public UserProfileEntity createProfile(UserEntity user, String timezone) {
    if (user == null) throw new IllegalArgumentException(USER_CANT_BE_NULL_MESSAGE);
    if (!TimezoneUtils.isValidTimezone(timezone))
      throw new IllegalArgumentException(INVALID_TIMEZONE_MESSAGE);
    UserProfileEntity userProfile = new UserProfileEntity();
    userProfile.setUser(user);
    userProfile.setTimezone(timezone);
    return userProfile;
  }

  @Override
  @Cacheable(value = "userProfiles", key = "#id")
  @Transactional(readOnly = true)
  public UserProfileDTO getById(UUID id) {
    return userProfileMapper.toUserProfileDTO(byId(id));
  }

  private UserProfileEntity byId(UUID id) {
    return userProfileRepository.findById(id).orElseThrow(UserProfileNotFoundException::new);
  }

  @Override
  @Transactional
  @CacheEvict(value = "userProfiles", key = "#id")
  public UserProfileDTO update(UUID id, UserProfileDTO userProfileDTO) {
    if (!validator.validate(userProfileDTO).isEmpty()) {
      throw new IllegalArgumentException(USER_PROFILE_DATA_NOT_VALID_MESSAGE);
    }
    UserProfileEntity profile = byId(id);
    BeanUtils.copyProperties(userProfileDTO, profile);
    UserProfileEntity updated = userProfileRepository.save(profile);
    return userProfileMapper.toUserProfileDTO(updated);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    UserEntity toDelete = byId(id).getUser();
    Instant now = Instant.now();
    toDelete.setDeletedAt(now);
    toDelete.getHabits().forEach(h -> h.setDeletedAt(now));
  }
}
