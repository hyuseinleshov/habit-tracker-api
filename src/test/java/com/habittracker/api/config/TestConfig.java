package com.habittracker.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {

  private static final String POSTGRES_IMAGE = "postgres:15-alpine";

  @Bean
  @ServiceConnection
  public PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
        .withReuse(true);
  }
}
