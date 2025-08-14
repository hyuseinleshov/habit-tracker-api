package com.habittracker.api.auth.testutils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import java.util.List;

@Component
public class TestDatabaseCleaner implements TestExecutionListener {

    private List<String> tableNames;

    @Override
    public void afterTestMethod(@NonNull TestContext testContext) {
        JdbcTemplate jdbcTemplate = jdbcTemplate(testContext);
        jdbcTemplate.execute("SET session_replication_role = 'replica';");

        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE");
        }

        jdbcTemplate.execute("SET session_replication_role = 'origin';");
    }

    @Override
    public void beforeTestClass(@NonNull TestContext testContext) {
        jdbcTemplate(testContext).execute("INSERT INTO roles (id, created_at, type) VALUES (gen_random_uuid(), now(), 'USER'), (gen_random_uuid(),now(), 'ADMIN') ON CONFLICT (type) DO NOTHING");
        tableNames =  jdbcTemplate(testContext).queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' AND table_name != 'roles'", String.class);

    }

    private static JdbcTemplate jdbcTemplate(TestContext testContext) {
        return testContext.getApplicationContext().getBean(JdbcTemplate.class);
    }
}