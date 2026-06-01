package com.test.integration;

import com.test.config.TestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MySQL and Redis multi-container integration demo")
class MultiContainerDemoIT extends TestContainerConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("MySQL and Redis containers work together")
    void shouldUseMysqlAndRedisTogether() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS container_probe (
                    id INT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL
                )
                """);
        jdbcTemplate.update(
                "INSERT INTO container_probe (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)",
                1,
                "mysql-ok");

        String mysqlValue = jdbcTemplate.queryForObject(
                "SELECT name FROM container_probe WHERE id = ?",
                String.class,
                1);

        redisTemplate.opsForValue().set("multi-container:status", "redis-ok");
        String redisValue = redisTemplate.opsForValue().get("multi-container:status");

        assertThat(mysqlContainer.isRunning()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(mysqlValue).isEqualTo("mysql-ok");
        assertThat(redisValue).isEqualTo("redis-ok");

        pauseForClassroomObservation();
    }

    private void pauseForClassroomObservation() {
        if (!Boolean.getBoolean("demo.pause")) {
            return;
        }
        try {
            Thread.sleep(15_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
