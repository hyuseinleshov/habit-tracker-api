package com.habittracker.api.user.service;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.user.dto.UserStatisticsResponse;

public interface UserStatisticsService {

  UserStatisticsResponse calculateStatistics(UserEntity user);
}
