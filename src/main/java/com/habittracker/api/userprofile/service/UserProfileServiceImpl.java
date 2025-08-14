package com.habittracker.api.userprofile.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void createProfile(UserEntity user, String timezone) {
        UserProfileEntity userProfile = new UserProfileEntity();
        userProfile.setUser(user);
        userProfile.setTimezone(timezone);
        userProfileRepository.save(userProfile);
    }
}
