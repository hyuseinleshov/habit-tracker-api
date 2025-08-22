package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;

public interface UserService {

  void updateEmail(UserEntity user, String email);

  void delete(UserEntity user);
}
