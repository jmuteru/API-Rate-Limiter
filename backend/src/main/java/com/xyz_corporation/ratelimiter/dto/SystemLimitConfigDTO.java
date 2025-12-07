package com.corporation.ratelimiter.dto;

import com.corporation.ratelimiter.model.SystemLimitConfig;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemLimitConfigDTO {
    @NotNull(message = "Global requests per second is required")
    @Min(value = 1, message = "Global requests per second must be at least 1")
    private Integer globalRequestsPerSecond;
    
    public SystemLimitConfig toEntity() {
        SystemLimitConfig config = new SystemLimitConfig();
        config.setGlobalRequestsPerSecond(this.globalRequestsPerSecond);
        return config;
    }
    
    public static SystemLimitConfigDTO fromEntity(SystemLimitConfig config) {
        SystemLimitConfigDTO dto = new SystemLimitConfigDTO();
        dto.setGlobalRequestsPerSecond(config.getGlobalRequestsPerSecond());
        return dto;
    }
}

