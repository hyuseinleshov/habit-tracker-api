package com.habittracker.api.userprofile.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileEntity createProfile(UserEntity user, String timezone) {
        UserProfileEntity userProfile = new UserProfileEntity();
        userProfile.setUser(user);
        userProfile.setTimezone(timezone);
        return userProfileRepository.save(userProfile);
    }
}
