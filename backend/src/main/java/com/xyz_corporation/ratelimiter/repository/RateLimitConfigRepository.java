package com.corporation.ratelimiter.repository;

import com.corporation.ratelimiter.model.RateLimitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfig, Long> {
    Optional<RateLimitConfig> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}

