package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.SystemLimitConfig;
import com.corporation.ratelimiter.repository.SystemLimitConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLimitConfigService {
    
    private final SystemLimitConfigRepository repository;
    
    @Value("${rate-limiter.global.requests-per-second:1000}")
    private int defaultGlobalRequestsPerSecond;
    
    public SystemLimitConfig getConfigOrDefault() {
        try {
            Optional<SystemLimitConfig> config = repository.findById(1L);
            if (config.isPresent()) {
                return config.get();
            }
            
            // create default config if not exists
            SystemLimitConfig defaultConfig = new SystemLimitConfig();
            defaultConfig.setId(1L);
            defaultConfig.setGlobalRequestsPerSecond(defaultGlobalRequestsPerSecond);
            return repository.save(defaultConfig);
        } catch (Exception e) {
            log.error("Error accessing database, returning default config in memory", e);
            // return in-memory default if database is unavailable
            SystemLimitConfig defaultConfig = new SystemLimitConfig();
            defaultConfig.setId(1L);
            defaultConfig.setGlobalRequestsPerSecond(defaultGlobalRequestsPerSecond);
            return defaultConfig;
        }
    }
    
    @Transactional
    public SystemLimitConfig updateConfig(SystemLimitConfig updatedConfig) {
        SystemLimitConfig existing = repository.findById(1L)
            .orElseGet(() -> {
                SystemLimitConfig newConfig = new SystemLimitConfig();
                newConfig.setId(1L);
                return newConfig;
            });
        
        existing.setGlobalRequestsPerSecond(updatedConfig.getGlobalRequestsPerSecond());
        return repository.save(existing);
    }
}

