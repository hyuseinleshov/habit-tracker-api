package com.habittracker.api.userprofile.dto;

import com.habittracker.api.core.validations.annotations.ValidTimezone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Length;

import static com.habittracker.api.userprofile.constants.UserProfileConstants.*;

public record UserProfileDTO(
    @Length(max = 50, message = INVALID_FIRST_NAME_MESSAGE) String firstName,
    @Length(max = 50, message = INVALID_LAST_NAME_MESSAGE) String lastName,
    @Min(value = 0, message = INVALID_AGE_MESSAGE) @Max(value = 150, message = INVALID_AGE_MESSAGE) Integer age,
    @ValidTimezone String timezone) {}
