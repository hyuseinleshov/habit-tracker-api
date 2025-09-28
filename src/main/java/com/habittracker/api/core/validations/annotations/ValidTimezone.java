package com.habittracker.api.core.validations.annotations;

import static com.habittracker.api.core.utils.TimeZoneUtils.INVALID_TIMEZONE_MESSAGE;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.habittracker.api.core.validations.validators.ValidTimezoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
@Constraint(validatedBy = ValidTimezoneValidator.class)
public @interface ValidTimezone {

  String message() default INVALID_TIMEZONE_MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
