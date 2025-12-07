package com.corporation.ratelimiter.controller;

import com.corporation.ratelimiter.dto.SystemLimitConfigDTO;
import com.corporation.ratelimiter.service.SystemLimitConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemLimitConfigController {
    
    private final SystemLimitConfigService systemLimitConfigService;
    
    @GetMapping("/limits")
    public ResponseEntity<SystemLimitConfigDTO> getSystemLimits() {
        return ResponseEntity.ok(
            SystemLimitConfigDTO.fromEntity(systemLimitConfigService.getConfigOrDefault())
        );
    }
    
    @PutMapping("/limits")
    public ResponseEntity<SystemLimitConfigDTO> updateSystemLimits(
            @Valid @RequestBody SystemLimitConfigDTO dto) {
        return ResponseEntity.ok(
            SystemLimitConfigDTO.fromEntity(systemLimitConfigService.updateConfig(dto.toEntity()))
        );
    }
}

