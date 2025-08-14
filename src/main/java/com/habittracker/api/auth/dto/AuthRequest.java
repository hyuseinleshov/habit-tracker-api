package com.habittracker.api.auth.dto;

import static com.habittracker.api.auth.utils.AuthConstants.*;

import com.habittracker.api.core.validations.annotations.ValidTimezone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record AuthRequest(
        @NotBlank(message = EMAIL_REQUIRED_MESSAGE) @Email(message = INVALID_EMAIL_MESSAGE) String email,
        @NotNull(message = PASSWORD_REQUIRED_MESSAGE) @Length(min = 6, message = PASSWORD_LENGTH_MESSAGE) String password,
        @ValidTimezone String timezone) {}
