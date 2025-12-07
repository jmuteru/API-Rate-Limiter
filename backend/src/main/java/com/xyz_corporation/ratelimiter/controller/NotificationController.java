package com.corporation.ratelimiter.controller;

import com.corporation.ratelimiter.dto.NotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    @PostMapping("/sms")
    public ResponseEntity<Map<String, String>> sendSMS(@Valid @RequestBody NotificationRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("type", "SMS");
        response.put("recipient", request.getRecipient());
        response.put("message", "SMS sent successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody NotificationRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("type", "EMAIL");
        response.put("recipient", request.getRecipient());
        response.put("message", "Email sent successfully");
        
        return ResponseEntity.ok(response);
    }
}

