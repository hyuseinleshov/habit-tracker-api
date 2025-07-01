package com.habittracker.api.security.jwt.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

  private String secret;
  private String issuer;
  private Duration expirationDuration;
  private Duration notBeforeLeewayDuration;
  private Integer clockSkewSeconds;
}
