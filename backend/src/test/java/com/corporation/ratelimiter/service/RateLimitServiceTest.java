package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.RateLimitConfig;
import com.corporation.ratelimiter.model.SystemLimitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RateLimitServiceTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private RateLimitConfigService configService;
    
    @Autowired
    private SystemLimitConfigService systemLimitConfigService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @BeforeEach
    void setUp() {
        // Clear Redis before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
    
    @Test
    void testTimeWindowLimit_WithinLimit() {
        String clientId = "test-client-1";
        
        // Create config with 10 requests per 60 seconds
        RateLimitConfig config = new RateLimitConfig();
        config.setClientId(clientId);
        config.setTimeWindowRequests(10);
        config.setTimeWindowSeconds(60);
        config.setMonthlyRequests(1000);
        configService.createConfig(config);
        
        // Make 5 requests - should all pass
        for (int i = 0; i < 5; i++) {
            RateLimitResult result = rateLimitService.checkTimeWindowLimit(clientId);
            assertTrue(result.isAllowed(), "Request " + i + " should be allowed");
            assertEquals(10, result.getLimit());
        }
    }
    
    @Test
    void testTimeWindowLimit_ExceedsLimit() {
        String clientId = "test-client-2";
        
        // Create config with 3 requests per 60 seconds
        RateLimitConfig config = new RateLimitConfig();
        config.setClientId(clientId);
        config.setTimeWindowRequests(3);
        config.setTimeWindowSeconds(60);
        config.setMonthlyRequests(1000);
        configService.createConfig(config);
        
        // Make 3 requests - should all pass
        for (int i = 0; i < 3; i++) {
            RateLimitResult result = rateLimitService.checkTimeWindowLimit(clientId);
            assertTrue(result.isAllowed());
        }
        
        // 4th request should fail
        RateLimitResult result = rateLimitService.checkTimeWindowLimit(clientId);
        assertFalse(result.isAllowed());
        assertEquals(4, result.getCurrentRequests());
        assertEquals(3, result.getLimit());
    }
    
    @Test
    void testMonthlyLimit_WithinLimit() {
        String clientId = "test-client-3";
        
        RateLimitConfig config = new RateLimitConfig();
        config.setClientId(clientId);
        config.setTimeWindowRequests(100);
        config.setTimeWindowSeconds(60);
        config.setMonthlyRequests(100);
        configService.createConfig(config);
        
        // Make 50 requests - should all pass
        for (int i = 0; i < 50; i++) {
            RateLimitResult result = rateLimitService.checkMonthlyLimit(clientId);
            assertTrue(result.isAllowed(), "Request " + i + " should be allowed");
        }
    }
    
    @Test
    void testGlobalLimit() {
        SystemLimitConfig config = new SystemLimitConfig();
        config.setGlobalRequestsPerSecond(10);
        systemLimitConfigService.updateConfig(config);
        
        // Make 10 requests - should all pass
        for (int i = 0; i < 10; i++) {
            RateLimitResult result = rateLimitService.checkGlobalLimit();
            assertTrue(result.isAllowed(), "Request " + i + " should be allowed");
        }
        
        // 11th request should fail
        RateLimitResult result = rateLimitService.checkGlobalLimit();
        assertFalse(result.isAllowed());
    }
}

