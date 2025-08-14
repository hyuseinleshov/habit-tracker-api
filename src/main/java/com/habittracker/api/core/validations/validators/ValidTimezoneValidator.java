package com.habittracker.api.core.validations.validators;

import com.habittracker.api.core.validations.annotations.ValidTimezone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;
import java.util.Set;

public class ValidTimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

    private static final Set<String> VALID_ZONE_IDS = ZoneId.getAvailableZoneIds();

    @Override
    public boolean isValid(String timezone, ConstraintValidatorContext context) {
        if (timezone == null) return false;
        return VALID_ZONE_IDS.contains(timezone.trim());
    }
}
