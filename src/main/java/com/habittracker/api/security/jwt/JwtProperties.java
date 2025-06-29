package com.habittracker.api.security.jwt;



import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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
