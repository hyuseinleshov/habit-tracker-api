package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_EXISTS_MESSAGE;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.mapper.UserProfileMapper;
import com.habittracker.api.user.model.UserProfileEntity;
import com.habittracker.api.user.repository.UserProfileRepository;
import com.habittracker.api.user.service.UserProfileService;
import com.habittracker.api.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserService userService;
  private final UserProfileMapper userProfileMapper;
  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public UserProfileEntity createProfile(UserEntity user, String timezone) {
    if (user == null) throw new IllegalArgumentException(USER_CANT_BE_NULL_MESSAGE);
    parseTimeZone(timezone);
    UserProfileEntity userProfile = new UserProfileEntity();
    userProfile.setUser(user);
    userProfile.setTimeZone(timezone);
    return userProfile;
  }

  @Override
  @Cacheable(value = "userProfiles", key = "#userId")
  @Transactional(readOnly = true)
  public UserProfileDTO toProfileDTO(UUID userId) {
    return userProfileMapper.toUserProfileDTO(userProfileRepository.getReferenceById(userId));
  }

  @Override
  @Transactional
  @CacheEvict(value = "userProfiles", key = "#userId")
  public UserProfileDTO update(UUID userId, UserProfileDTO userProfileDTO) {
    UserProfileEntity profile = userProfileRepository.getReferenceById(userId);

    if (userProfileDTO.email() != null) {
      String currentEmail = profile.getUser().getEmail();
      if (!currentEmail.equals(userProfileDTO.email())) {
        assertEmailNotTaken(userProfileDTO.email());
        userService.updateEmail(profile.getUser(), userProfileDTO.email());
      }
    }

    userProfileMapper.updateProfileFromDto(userProfileDTO, profile);
    return userProfileMapper.toUserProfileDTO(profile);
  }

  private void assertEmailNotTaken(String email) {
    userRepository
        .findByEmailAndDeletedAtIsNull(email)
        .ifPresent(
            u -> {
              throw new IllegalArgumentException(EMAIL_EXISTS_MESSAGE);
            });
  }
}
