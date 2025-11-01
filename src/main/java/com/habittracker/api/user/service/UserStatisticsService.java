package com.habittracker.api.user.service;

import com.habittracker.api.user.dto.UserStatisticsResponse;
import java.util.UUID;

public interface UserStatisticsService {

  UserStatisticsResponse calculateStatistics(UUID id);
}
