package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.RateLimitConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitResult {
    private boolean allowed;
    private long currentRequests;
    private long limit;
    private long remainingRequests;
    private RateLimitConfig.ThrottlingMode throttlingMode;
}

