package com.habittracker.api.userprofile.service;

import com.habittracker.api.auth.model.UserEntity;

public interface UserProfileService {

    void createProfile(UserEntity user, String timezone);
}
