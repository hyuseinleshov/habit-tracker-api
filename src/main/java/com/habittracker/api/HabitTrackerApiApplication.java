package com.habittracker.api;

import com.habittracker.api.security.jwt.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(JwtProperties.class)
public class HabitTrackerApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(HabitTrackerApiApplication.class, args);
  }
}
