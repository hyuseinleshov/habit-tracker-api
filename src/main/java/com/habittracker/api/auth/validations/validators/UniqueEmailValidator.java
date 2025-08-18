package com.habittracker.api.auth.validations.validators;

import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.validations.annotations.UniqueEmail;
import com.habittracker.api.core.utils.TimezoneUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_EXISTS_MESSAGE;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

  private final UserRepository userRepository;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
     if(email == null) return true;
     return userRepository.findByEmail(email)
                     .map(u -> {
                         if(!u.isDeleted()) {
                             setMessage(EMAIL_EXISTS_MESSAGE, context);
                         } else  {
                             String message = "This email was previously used by a deleted account. You can register with it again after "
                                     + u.getDeletedAt().atZone(ZoneId.of(u.getUserProfile().getTimezone()))
                                             .plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                             setMessage(message, context);
                         }
                         return false;
                     }).orElse(true);
  }

  private void setMessage(String message, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }


}
