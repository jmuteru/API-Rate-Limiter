package com.corporation.ratelimiter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String clientId;
    
    // time window rate limit
    private Integer timeWindowRequests;
    private Integer timeWindowSeconds;
    
    // monthly rate limit
    private Integer monthlyRequests;
    
    // throttling mode
    @Enumerated(EnumType.STRING)
    private ThrottlingMode throttlingMode = ThrottlingMode.HARD;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ThrottlingMode {
        SOFT, // warning headers but allow request
        HARD  // reject with  code 429. rate limit has been exceeded
    }
}

