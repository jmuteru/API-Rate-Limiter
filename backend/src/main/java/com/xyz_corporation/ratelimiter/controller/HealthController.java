package com.corporation.ratelimiter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            if (healthEndpoint != null) {
                // Use actuator health endpoint
                HealthComponent healthComponent = healthEndpoint.health();
                
                // Check if it's a Health object
                if (healthComponent instanceof org.springframework.boot.actuate.health.Health) {
                    org.springframework.boot.actuate.health.Health health = 
                        (org.springframework.boot.actuate.health.Health) healthComponent;
                    Status status = health.getStatus();
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", status.getCode());
                    response.put("details", health.getDetails());
                    return ResponseEntity.ok(response);
                } else {
                    // If it's just a Status
                    Status status = healthComponent.getStatus();
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", status.getCode());
                    return ResponseEntity.ok(response);
                }
            } else {
                // Fallback if actuator is not available
                Map<String, Object> response = new HashMap<>();
                response.put("status", "UP");
                response.put("message", "Service is running");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(503).body(response);
        }
    }
}

