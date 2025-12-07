package com.corporation.ratelimiter.dto;

import com.corporation.ratelimiter.model.RateLimitConfig;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateLimitConfigDTO {
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotNull(message = "Time window requests is required")
    @Min(value = 1, message = "Time window requests must be at least 1")
    private Integer timeWindowRequests;
    
    @NotNull(message = "Time window seconds is required")
    @Min(value = 1, message = "Time window seconds must be at least 1")
    private Integer timeWindowSeconds;
    
    @NotNull(message = "Monthly requests is required")
    @Min(value = 1, message = "Monthly requests must be at least 1")
    private Integer monthlyRequests;
    
    private RateLimitConfig.ThrottlingMode throttlingMode = RateLimitConfig.ThrottlingMode.HARD;
    
    public RateLimitConfig toEntity() {
        RateLimitConfig config = new RateLimitConfig();
        config.setClientId(this.clientId);
        config.setTimeWindowRequests(this.timeWindowRequests);
        config.setTimeWindowSeconds(this.timeWindowSeconds);
        config.setMonthlyRequests(this.monthlyRequests);
        config.setThrottlingMode(this.throttlingMode);
        return config;
    }
    
    public static RateLimitConfigDTO fromEntity(RateLimitConfig config) {
        RateLimitConfigDTO dto = new RateLimitConfigDTO();
        dto.setClientId(config.getClientId());
        dto.setTimeWindowRequests(config.getTimeWindowRequests());
        dto.setTimeWindowSeconds(config.getTimeWindowSeconds());
        dto.setMonthlyRequests(config.getMonthlyRequests());
        dto.setThrottlingMode(config.getThrottlingMode());
        return dto;
    }
}

