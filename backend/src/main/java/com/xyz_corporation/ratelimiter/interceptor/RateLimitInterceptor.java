package com.corporation.ratelimiter.interceptor;

import com.corporation.ratelimiter.model.RateLimitConfig;
import com.corporation.ratelimiter.service.RateLimitResult;
import com.corporation.ratelimiter.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitService rateLimitService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // skip rate limiting for configuration endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/rate-limits") || 
            path.startsWith("/api/system") ||
            path.startsWith("/api/clients") ||
            path.startsWith("/actuator") ||
            path.startsWith("/h2-console")) {
            return true;
        }
        
        // get clientID from header or use ip address as fallback
        String clientId = request.getHeader("X-Client-Id");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr();
        }
        
        try {
            // first I check the global limit
            RateLimitResult globalResult = rateLimitService.checkGlobalLimit();
            if (!globalResult.isAllowed()) {
                return handleRateLimitExceeded(response, globalResult, "Global rate limit exceeded");
            }
            
            // check time window limit
            RateLimitResult timeWindowResult = rateLimitService.checkTimeWindowLimit(clientId);
            if (!timeWindowResult.isAllowed()) {
                return handleRateLimitExceeded(response, timeWindowResult, "Time window rate limit exceeded");
            }
            
            // check monthly limit
            RateLimitResult monthlyResult = rateLimitService.checkMonthlyLimit(clientId);
            if (!monthlyResult.isAllowed()) {
                return handleRateLimitExceeded(response, monthlyResult, "Monthly rate limit exceeded");
            }
            
            // add rate limit headers
            addRateLimitHeaders(response, timeWindowResult, monthlyResult, globalResult);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limits", e);
            // allow request if error occurs but log it
            return true;
        }
    }
    
    private boolean handleRateLimitExceeded(HttpServletResponse response, 
                                           RateLimitResult result, 
                                           String message) {
        if (result.getThrottlingMode() == RateLimitConfig.ThrottlingMode.HARD) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 60));
            // reject the request
            return false; 
        } else {
            // soft throttling with warnings in the headers. also allow the request
            response.setHeader("X-RateLimit-Warning", message);
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
            return true; // Allow the request
        }
    }
    
    private void addRateLimitHeaders(HttpServletResponse response,
                                    RateLimitResult timeWindowResult,
                                    RateLimitResult monthlyResult,
                                    RateLimitResult globalResult) {
        response.setHeader("X-RateLimit-TimeWindow-Limit", String.valueOf(timeWindowResult.getLimit()));
        response.setHeader("X-RateLimit-TimeWindow-Remaining", String.valueOf(timeWindowResult.getRemainingRequests()));
        response.setHeader("X-RateLimit-Monthly-Limit", String.valueOf(monthlyResult.getLimit()));
        response.setHeader("X-RateLimit-Monthly-Remaining", String.valueOf(monthlyResult.getRemainingRequests()));
        response.setHeader("X-RateLimit-Global-Limit", String.valueOf(globalResult.getLimit()));
        response.setHeader("X-RateLimit-Global-Remaining", String.valueOf(globalResult.getRemainingRequests()));
    }
}

