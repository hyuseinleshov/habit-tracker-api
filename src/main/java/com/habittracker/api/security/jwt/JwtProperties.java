package com.habittracker.api.security.jwt;



import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class JwtProperties {

    private String secret;
    private String issuer;
    private Duration expirationDuration;
    private Duration notBeforeLeewayDuration;
    private Integer clockSkewSeconds;

}
