package com.habittracker.api;

import static com.habittracker.api.habit.specs.HabitSpecs.*;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.habit.repository.HabitRepository;
import com.habittracker.api.security.jwt.config.JwtProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class HabitTrackerApiApplication {

  private final HabitRepository habitRepository;
  private final UserRepository userRepository;

  public HabitTrackerApiApplication(
      HabitRepository habitRepository, UserRepository userRepository) {
    this.habitRepository = habitRepository;
    this.userRepository = userRepository;
  }

  public static void main(String[] args) {
    SpringApplication.run(HabitTrackerApiApplication.class, args);
  }

  @Bean
  public CommandLineRunner runner() {
    return args ->
        System.out.println(
            habitRepository.findAll(
                hasUser(userRepository.findAll().getFirst())
                    .and(isDeleted(false).and(isArchived(false)))));
  }
}
