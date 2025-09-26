package com.habittracker.api.checkin.service;

import com.habittracker.api.checkin.CheckInResponse;
import java.util.UUID;

public interface CheckInService {

  CheckInResponse checkIn(UUID habitId, String userTimezone);
}
