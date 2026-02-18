package com.habittracker.api.checkin.dto;

import java.time.Instant;
import java.util.UUID;

public record CheckInResponse(UUID id, UUID habitId, Instant createdAt) {}
