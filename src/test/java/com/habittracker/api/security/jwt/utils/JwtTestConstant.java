package com.habittracker.api.security.jwt.utils;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

public class JwtTestConstant {

    public static final SecretKey TEST_SECRET_KEY = Jwts.SIG.HS256.key().build();
    public static final String ISSUER = "test-issuer";
}
