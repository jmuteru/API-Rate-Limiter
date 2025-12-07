package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.RateLimitConfig;
import com.corporation.ratelimiter.model.SystemLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfigService configService;
    private final SystemLimitConfigService systemLimitConfigService;
    
    private static final String TIME_WINDOW_PREFIX = "rate_limit:time_window:";
    private static final String MONTHLY_PREFIX = "rate_limit:monthly:";
    private static final String GLOBAL_PREFIX = "rate_limit:global:";
    
    // redis: script for atomic time window rate limiting
    private static final String TIME_WINDOW_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "  redis.call('EXPIRE', key, window) " +
        "end " +
        "return {current, limit}";
    
    // redis: script for monthly rate limiting
    private static final String MONTHLY_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local current = redis.call('INCR', key) " +
        "local ttl = redis.call('TTL', key) " +
        "if ttl == -1 then " +
        "  local now = tonumber(ARGV[2]) " +
        "  local monthStart = tonumber(ARGV[3]) " +
        "  local secondsUntilMonthEnd = tonumber(ARGV[4]) " +
        "  redis.call('EXPIRE', key, secondsUntilMonthEnd) " +
        "end " +
        "return {current, limit}";
    
    // redis: script for global rate limiting
    private static final String GLOBAL_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "  redis.call('EXPIRE', key, 1) " +
        "end " +
        "return {current, limit}";
    
    public RateLimitResult checkTimeWindowLimit(String clientId) {
        try {
            RateLimitConfig config = configService.getConfigOrDefault(clientId);
            
            String key = TIME_WINDOW_PREFIX + clientId;
            int limit = config.getTimeWindowRequests();
            int window = config.getTimeWindowSeconds();
            
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(TIME_WINDOW_SCRIPT);
            script.setResultType(List.class);
            
            @SuppressWarnings("unchecked")
            List<Long> result = redisTemplate.execute(script, 
                Collections.singletonList(key), 
                String.valueOf(limit), 
                String.valueOf(window));
            
            if (result == null || result.isEmpty()) {
                log.warn("Redis script returned null or empty result for client {}", clientId);
                return createAllowResult(limit, config.getThrottlingMode());
            }
            
            long current = result.get(0);
            boolean allowed = current <= limit;
            
            return RateLimitResult.builder()
                .allowed(allowed)
                .currentRequests(current)
                .limit(limit)
                .remainingRequests(Math.max(0, limit - current))
                .throttlingMode(config.getThrottlingMode())
                .build();
        } catch (Exception e) {
            log.error("Error checking time window limit for client {}: {}", clientId, e.getMessage(), e);
            // allow request if Redis/database is unavailable
            RateLimitConfig config = configService.getConfigOrDefault(clientId);
            return createAllowResult(config.getTimeWindowRequests(), config.getThrottlingMode());
        }
    }
    
    private RateLimitResult createAllowResult(int limit, RateLimitConfig.ThrottlingMode mode) {
        return RateLimitResult.builder()
            .allowed(true)
            .currentRequests(0)
            .limit(limit)
            .remainingRequests(limit)
            .throttlingMode(mode)
            .build();
    }
    
    public RateLimitResult checkMonthlyLimit(String clientId) {
        try {
            RateLimitConfig config = configService.getConfigOrDefault(clientId);
            
            String key = MONTHLY_PREFIX + clientId + ":" + getCurrentMonthKey();
            int limit = config.getMonthlyRequests();
            
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(MONTHLY_SCRIPT);
            script.setResultType(List.class);
            
            long now = Instant.now().getEpochSecond();
            long monthStart = getMonthStartTimestamp();
            long secondsUntilMonthEnd = getSecondsUntilMonthEnd();
            
            @SuppressWarnings("unchecked")
            List<Long> result = redisTemplate.execute(script, 
                Collections.singletonList(key), 
                String.valueOf(limit),
                String.valueOf(now),
                String.valueOf(monthStart),
                String.valueOf(secondsUntilMonthEnd));
            
            if (result == null || result.isEmpty()) {
                log.warn("Redis script returned null or empty result for monthly limit, client {}", clientId);
                return createAllowResult(limit, config.getThrottlingMode());
            }
            
            long current = result.get(0);
            boolean allowed = current <= limit;
            
            return RateLimitResult.builder()
                .allowed(allowed)
                .currentRequests(current)
                .limit(limit)
                .remainingRequests(Math.max(0, limit - current))
                .throttlingMode(config.getThrottlingMode())
                .build();
        } catch (Exception e) {
            log.error("Error checking monthly limit for client {}: {}", clientId, e.getMessage(), e);
            // allow request if Redis/database is unavailable
            RateLimitConfig config = configService.getConfigOrDefault(clientId);
            return createAllowResult(config.getMonthlyRequests(), config.getThrottlingMode());
        }
    }
    
    public RateLimitResult checkGlobalLimit() {
        try {
            SystemLimitConfig config = systemLimitConfigService.getConfigOrDefault();
            
            String key = GLOBAL_PREFIX + "second:" + getCurrentSecond();
            int limit = config.getGlobalRequestsPerSecond();
            
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(GLOBAL_SCRIPT);
            script.setResultType(List.class);
            
            @SuppressWarnings("unchecked")
            List<Long> result = redisTemplate.execute(script, 
                Collections.singletonList(key), 
                String.valueOf(limit));
            
            if (result == null || result.isEmpty()) {
                log.warn("Redis script returned null or empty result for global limit");
                return createAllowResult(limit, RateLimitConfig.ThrottlingMode.HARD);
            }
            
            long current = result.get(0);
            boolean allowed = current <= limit;
            
            return RateLimitResult.builder()
                .allowed(allowed)
                .currentRequests(current)
                .limit(limit)
                .remainingRequests(Math.max(0, limit - current))
                .throttlingMode(RateLimitConfig.ThrottlingMode.HARD)
                .build();
        } catch (Exception e) {
            log.error("Error checking global limit: {}", e.getMessage(), e);
            // allow request if Redis/database is unavailable
            SystemLimitConfig config = systemLimitConfigService.getConfigOrDefault();
            return createAllowResult(config.getGlobalRequestsPerSecond(), RateLimitConfig.ThrottlingMode.HARD);
        }
    }
    
    private String getCurrentMonthKey() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
    
    private long getMonthStartTimestamp() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        return monthStart.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }
    
    private long getSecondsUntilMonthEnd() {
        LocalDate now = LocalDate.now();
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        long monthEndTimestamp = monthEnd.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);
        return monthEndTimestamp - Instant.now().getEpochSecond();
    }
    
    private String getCurrentSecond() {
        return String.valueOf(Instant.now().getEpochSecond());
    }
}

