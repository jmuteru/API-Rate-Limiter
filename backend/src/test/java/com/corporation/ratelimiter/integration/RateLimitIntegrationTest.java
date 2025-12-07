package com.corporation.ratelimiter.integration;

import com.corporation.ratelimiter.dto.NotificationRequest;
import com.corporation.ratelimiter.dto.RateLimitConfigDTO;
import com.corporation.ratelimiter.model.RateLimitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testRateLimiting_TimeWindow() throws Exception {
        // Create rate limit config: 3 requests per 60 seconds
        RateLimitConfigDTO configDTO = new RateLimitConfigDTO();
        configDTO.setClientId("integration-client");
        configDTO.setTimeWindowRequests(3);
        configDTO.setTimeWindowSeconds(60);
        configDTO.setMonthlyRequests(1000);
        configDTO.setThrottlingMode(RateLimitConfig.ThrottlingMode.HARD);
        
        mockMvc.perform(post("/api/rate-limits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configDTO)))
                .andExpect(status().isCreated());
        
        // Make 3 requests - should all succeed
        NotificationRequest notification = new NotificationRequest();
        notification.setRecipient("test@example.com");
        notification.setMessage("Test message");
        
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/notifications/email")
                    .header("X-Client-Id", "integration-client")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-RateLimit-TimeWindow-Remaining"));
        }
        
        // 4th request should be rejected
        mockMvc.perform(post("/api/notifications/email")
                .header("X-Client-Id", "integration-client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isTooManyRequests());
    }
    
    @Test
    void testSoftThrottling() throws Exception {
        // Create config with soft throttling
        RateLimitConfigDTO configDTO = new RateLimitConfigDTO();
        configDTO.setClientId("soft-throttle-client");
        configDTO.setTimeWindowRequests(2);
        configDTO.setTimeWindowSeconds(60);
        configDTO.setMonthlyRequests(1000);
        configDTO.setThrottlingMode(RateLimitConfig.ThrottlingMode.SOFT);
        
        mockMvc.perform(post("/api/rate-limits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configDTO)))
                .andExpect(status().isCreated());
        
        NotificationRequest notification = new NotificationRequest();
        notification.setRecipient("test@example.com");
        notification.setMessage("Test message");
        
        // Make 2 requests - should succeed
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/notifications/email")
                    .header("X-Client-Id", "soft-throttle-client")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk());
        }
        
        // 3rd request should still succeed but with warning header
        mockMvc.perform(post("/api/notifications/email")
                .header("X-Client-Id", "soft-throttle-client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Warning"));
    }
}

