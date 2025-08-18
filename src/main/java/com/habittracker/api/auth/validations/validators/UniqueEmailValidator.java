package com.habittracker.api.auth.validations.validators;

import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_DELETED_MESSAGE;
import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_EXISTS_MESSAGE;

import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.validations.annotations.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

  private final UserRepository userRepository;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null) return true;
    return userRepository
        .findByEmail(email)
        .map(
            u -> {
              if (!u.isDeleted()) {
                setMessage(EMAIL_EXISTS_MESSAGE, context);
              } else {
                String deleteDate =
                    u.getDeletedAt()
                        .atZone(ZoneId.of(u.getUserProfile().getTimezone()))
                        .plusMonths(1)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);
                String message = String.format(EMAIL_DELETED_MESSAGE, deleteDate);
                setMessage(message, context);
              }
              return false;
            })
        .orElse(true);
  }

  private void setMessage(String message, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
