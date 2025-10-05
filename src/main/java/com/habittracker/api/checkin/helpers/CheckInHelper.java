package com.habittracker.api.checkin.helpers;

import com.habittracker.api.checkin.repository.CheckInRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckInHelper {

  private final CheckInRepository checkInRepository;

  public boolean isOwnedByUser(UUID checkInId, UUID userId) {
    return checkInRepository.existsByIdAndHabitUserId(checkInId, userId);
  }
}
