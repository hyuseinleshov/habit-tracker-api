package com.habittracker.api.config;

import com.habittracker.api.auth.controller.AuthController;
import com.habittracker.api.auth.exception.AuthExceptionHandler;
import com.habittracker.api.core.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for controller slice tests. Disables CSRF and configures stateless
 * sessions to facilitate testing.
 */
@Configuration
@Import({AuthController.class, AuthExceptionHandler.class, GlobalExceptionHandler.class})
public class SecurityTestConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
