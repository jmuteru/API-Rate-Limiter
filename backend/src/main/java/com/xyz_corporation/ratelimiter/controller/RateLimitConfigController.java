package com.corporation.ratelimiter.controller;

import com.corporation.ratelimiter.dto.RateLimitConfigDTO;
import com.corporation.ratelimiter.model.RateLimitConfig;
import com.corporation.ratelimiter.service.RateLimitConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rate-limits")
@RequiredArgsConstructor
public class RateLimitConfigController {
    
    private final RateLimitConfigService configService;
    
    @PostMapping
    public ResponseEntity<?> createConfig(@Valid @RequestBody RateLimitConfigDTO dto) {
        try {
            RateLimitConfig config = configService.createConfig(dto.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED).body(RateLimitConfigDTO.fromEntity(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage(), "status", HttpStatus.CONFLICT.value()));
        }
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<RateLimitConfigDTO> getConfig(@PathVariable String clientId) {
        return configService.getConfig(clientId)
            .map(config -> ResponseEntity.ok(RateLimitConfigDTO.fromEntity(config)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<RateLimitConfigDTO>> getAllConfigs() {
        List<RateLimitConfigDTO> configs = configService.getAllConfigs().stream()
            .map(RateLimitConfigDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/{clientId}")
    public ResponseEntity<RateLimitConfigDTO> updateConfig(
            @PathVariable String clientId,
            @Valid @RequestBody RateLimitConfigDTO dto) {
        RateLimitConfig config = configService.updateConfig(clientId, dto.toEntity());
        return ResponseEntity.ok(RateLimitConfigDTO.fromEntity(config));
    }
    
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String clientId) {
        configService.deleteConfig(clientId);
        return ResponseEntity.noContent().build();
    }
}

