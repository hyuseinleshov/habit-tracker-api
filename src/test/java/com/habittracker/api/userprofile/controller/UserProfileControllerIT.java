package com.habittracker.api.userprofile.controller;

import com.habittracker.api.auth.dto.RegisterRequest;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.security.jwt.config.JwtProperties;
import com.habittracker.api.security.jwt.testutils.JwtTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import static com.habittracker.api.auth.testutils.MockMvcTestUtils.addJwt;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static com.habittracker.api.config.constants.JwtTestConstant.AUTHORIZATION_HEADER;
import static com.habittracker.api.config.constants.JwtTestConstant.BEARER_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@BaseIntegrationTest
public class UserProfileControllerIT {

    @Autowired
    private SecretKey secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    public void test_Get_UserProfile_Return_Unauthorized_When_NotHave_Jwt() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void test_getUserProfile_Return_ExpectedResult_With_JWT() throws Exception {
        authService.register(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE));
        String jwt = JwtTestUtils.generateValidToken(TEST_EMAIL, issuer, secretKey);
        mockMvc.perform(addJwt(jwt, get("/api/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value(TEST_TIMEZONE));
    }


}