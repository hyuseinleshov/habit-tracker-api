package com.habittracker.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

  @Bean
  @ServiceConnection("postgres")
  public PostgreSQLContainer postgreSQLContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"));
  }

  @Bean
  @ServiceConnection("redis")
  @SuppressWarnings("resource")
  public GenericContainer<?> redisContainer() {
    return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
  }
}
