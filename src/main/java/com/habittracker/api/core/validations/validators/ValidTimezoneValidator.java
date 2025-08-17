package com.habittracker.api.core.validations.validators;

import com.habittracker.api.core.utils.TimezoneUtils;
import com.habittracker.api.core.validations.annotations.ValidTimezone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

  @Override
  public boolean isValid(String timezone, ConstraintValidatorContext context) {
    return TimezoneUtils.isValidTimezone(timezone);
  }
}
