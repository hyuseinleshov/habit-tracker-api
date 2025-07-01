package com.habittracker.api.security.jwt.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

  @NotBlank private String secret;
  @NotBlank private String issuer;
  @NotNull private Duration expirationDuration;
  @NotNull private Duration notBeforeLeewayDuration;

  @NotNull @Min(1) private Integer clockSkewSeconds;
}
