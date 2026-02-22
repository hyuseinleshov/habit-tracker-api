package com.habittracker.api.core.validations.validators;

import com.habittracker.api.core.utils.TimeZoneUtils;
import com.habittracker.api.core.validations.annotations.ValidTimeZone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTimeZoneValidator implements ConstraintValidator<ValidTimeZone, String> {

  @Override
  public boolean isValid(String timezone, ConstraintValidatorContext context) {
    if (timezone == null) return true;
    return TimeZoneUtils.isValidTimeZone(timezone);
  }
}
