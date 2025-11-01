package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;
import java.util.UUID;

public interface UserService {

  void updateEmail(UserEntity user, String email);

  void delete(UUID userId);
}
