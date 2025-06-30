package com.habittracker.api.security.jwt;

import javax.crypto.SecretKey;

public class JwtServiceImpl implements JwtService{

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtServiceImpl(SecretKey secretKey, JwtProperties jwtProperties) {
        this.secretKey = secretKey;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generateToken(String email) {
        return "";
    }
}
