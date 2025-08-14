package com.habittracker.api.userprofile.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import org.springframework.security.core.userdetails.User;

public interface UserProfileService {

    UserProfileEntity createProfile(UserEntity user, String timezone);
}
