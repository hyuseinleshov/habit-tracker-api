package com.habittracker.api.auth.dto;

import static com.habittracker.api.auth.utils.AuthConstants.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
    @NotBlank(message = EMAIL_REQUIRED_MESSAGE) @Email(message = INVALID_EMAIL_MESSAGE) String email,
    @NotBlank(message = PASSWORD_REQUIRED_MESSAGE) @Size(min = 6, message = PASSWORD_LENGTH_MESSAGE) String password) {}
