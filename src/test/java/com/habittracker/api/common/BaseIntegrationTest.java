package com.habittracker.api.common;

import com.habittracker.api.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseIntegrationTest {
  // Common test utilities can be added here
}
