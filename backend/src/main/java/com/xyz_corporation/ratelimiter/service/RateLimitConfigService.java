package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.RateLimitConfig;
import com.corporation.ratelimiter.repository.RateLimitConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitConfigService {
    
    private final RateLimitConfigRepository repository;
    
    @Value("${rate-limiter.default.time-window.requests:100}")
    private int defaultTimeWindowRequests;
    
    @Value("${rate-limiter.default.time-window.window-seconds:60}")
    private int defaultTimeWindowSeconds;
    
    @Value("${rate-limiter.default.monthly.requests:10000}")
    private int defaultMonthlyRequests;
    
    public RateLimitConfig createConfig(RateLimitConfig config) {
        if (repository.existsByClientId(config.getClientId())) {
            throw new IllegalArgumentException("Rate limit config already exists for client: " + config.getClientId());
        }
        return repository.save(config);
    }
    
    public Optional<RateLimitConfig> getConfig(String clientId) {
        return repository.findByClientId(clientId);
    }
    
    public RateLimitConfig getConfigOrDefault(String clientId) {
        return repository.findByClientId(clientId)
            .orElseGet(() -> createDefaultConfig(clientId));
    }
    
    private RateLimitConfig createDefaultConfig(String clientId) {
        RateLimitConfig config = new RateLimitConfig();
        config.setClientId(clientId);
        config.setTimeWindowRequests(defaultTimeWindowRequests);
        config.setTimeWindowSeconds(defaultTimeWindowSeconds);
        config.setMonthlyRequests(defaultMonthlyRequests);
        config.setThrottlingMode(RateLimitConfig.ThrottlingMode.HARD);
        return config;
    }
    
    @Transactional
    public RateLimitConfig updateConfig(String clientId, RateLimitConfig updatedConfig) {
        RateLimitConfig existing = repository.findByClientId(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Rate limit config not found for client: " + clientId));
        
        existing.setTimeWindowRequests(updatedConfig.getTimeWindowRequests());
        existing.setTimeWindowSeconds(updatedConfig.getTimeWindowSeconds());
        existing.setMonthlyRequests(updatedConfig.getMonthlyRequests());
        existing.setThrottlingMode(updatedConfig.getThrottlingMode());
        
        return repository.save(existing);
    }
    
    public void deleteConfig(String clientId) {
        repository.findByClientId(clientId)
            .ifPresent(repository::delete);
    }
    
    public List<RateLimitConfig> getAllConfigs() {
        return repository.findAll();
    }
}

