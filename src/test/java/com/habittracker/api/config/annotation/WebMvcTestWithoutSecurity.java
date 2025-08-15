package com.habittracker.api.config.annotation;

import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.SecurityTestConfig;
import com.habittracker.api.security.jwt.filter.JwtFilter;
import java.lang.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@Import({SecurityTestConfig.class, MockMvcTestUtils.class})
@ActiveProfiles("test")
public @interface WebMvcTestWithoutSecurity {

  @AliasFor(annotation = WebMvcTest.class)
  Class<?>[] value() default {};
}
