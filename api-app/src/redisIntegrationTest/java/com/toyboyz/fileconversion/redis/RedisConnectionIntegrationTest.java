package com.toyboyz.fileconversion.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",

                "SLACK_WEBHOOK_URL=http://localhost/dummy",

                "S3_BUCKET=test-bucket",
                "AWS_REGION=ap-northeast-2",
                "S3_PREFIX=uploads",
                "S3_PRESIGN_TTL_SECONDS=900",

                "SPRING_RABBITMQ_HOST=localhost",
                "SPRING_RABBITMQ_PORT=5672",
                "SPRING_RABBITMQ_USERNAME=test",
                "SPRING_RABBITMQ_PASSWORD=test",

                "SPRING_DATA_REDIS_DATABASE=0"
        }
)
class RedisConnectionIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void redis_set_and_get_should_work() {
        stringRedisTemplate.opsForValue().set("key", "value");
        assertThat(stringRedisTemplate.opsForValue().get("key")).isEqualTo("value");
    }
}