package com.habittracker.api.auth.validations.annotations;

import com.habittracker.api.auth.validations.validators.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_REQUIRED_MESSAGE;
import static com.habittracker.api.userprofile.constants.UserProfileConstants.INVALID_TIMEZONE_MESSAGE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {

  String message() default EMAIL_REQUIRED_MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
