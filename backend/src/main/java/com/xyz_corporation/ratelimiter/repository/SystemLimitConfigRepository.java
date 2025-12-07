package com.corporation.ratelimiter.repository;

import com.corporation.ratelimiter.model.SystemLimitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLimitConfigRepository extends JpaRepository<SystemLimitConfig, Long> {
}

