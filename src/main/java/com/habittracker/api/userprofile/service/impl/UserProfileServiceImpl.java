package com.habittracker.api.userprofile.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.mapper.UserProfileMapper;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import com.habittracker.api.userprofile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public void createProfile(UserEntity user, String timezone) {
        UserProfileEntity userProfile = new UserProfileEntity();
        userProfile.setUser(user);
        userProfile.setTimezone(timezone);
        userProfileRepository.save(userProfile);
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#id")
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileById(UUID id) {
        return userProfileMapper.toUserProfileDTO(userProfileRepository.getReferenceById(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#id")
    public UserProfileDTO updateUserProfile(UUID id, UserProfileDTO userProfileDTO) {
        UserProfileEntity profile = userProfileRepository.getReferenceById(id);
        BeanUtils.copyProperties(userProfileDTO, profile);
        UserProfileEntity updated = userProfileRepository.save(profile);
        return userProfileMapper.toUserProfileDTO(updated);
    }
}
