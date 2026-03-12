package com.toyboyz.fileconversion.redis;

import com.redis.testcontainers.RedisContainer;
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
@SpringBootTest
class RedisConnectionIntegrationTest {

//    @Container
//    static RedisContainer redis = new RedisContainer("redis:7.2");
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
//        String key = "ci:test:key";
//        String value = "hello-redis";
//
//        stringRedisTemplate.opsForValue().set(key, value);
//        String result = stringRedisTemplate.opsForValue().get(key);
//
//        assertThat(result).isEqualTo(value);
        stringRedisTemplate.opsForValue().set("key", "value");
        assertThat(stringRedisTemplate.opsForValue().get("key")).isEqualTo("value");
    }
}