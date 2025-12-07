package com.corporation.ratelimiter.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitConfigControllerTest {
    
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
    void testCreateConfig() throws Exception {
        RateLimitConfigDTO dto = new RateLimitConfigDTO();
        dto.setClientId("test-client");
        dto.setTimeWindowRequests(100);
        dto.setTimeWindowSeconds(60);
        dto.setMonthlyRequests(10000);
        dto.setThrottlingMode(RateLimitConfig.ThrottlingMode.HARD);
        
        mockMvc.perform(post("/api/rate-limits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andExpect(jsonPath("$.timeWindowRequests").value(100));
    }
    
    @Test
    void testGetConfig() throws Exception {
        // First create a config
        RateLimitConfigDTO dto = new RateLimitConfigDTO();
        dto.setClientId("test-client-get");
        dto.setTimeWindowRequests(100);
        dto.setTimeWindowSeconds(60);
        dto.setMonthlyRequests(10000);
        
        mockMvc.perform(post("/api/rate-limits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        
        // Then get it
        mockMvc.perform(get("/api/rate-limits/test-client-get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("test-client-get"));
    }
    
    @Test
    void testUpdateConfig() throws Exception {
        // Create config
        RateLimitConfigDTO dto = new RateLimitConfigDTO();
        dto.setClientId("test-client-update");
        dto.setTimeWindowRequests(100);
        dto.setTimeWindowSeconds(60);
        dto.setMonthlyRequests(10000);
        
        mockMvc.perform(post("/api/rate-limits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        
        // Update config
        dto.setTimeWindowRequests(200);
        mockMvc.perform(put("/api/rate-limits/test-client-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeWindowRequests").value(200));
    }
}

