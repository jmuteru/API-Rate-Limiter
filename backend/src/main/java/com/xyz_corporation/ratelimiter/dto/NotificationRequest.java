package com.corporation.ratelimiter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank(message = "Recipient is required") //email
    private String recipient;
    
    @NotBlank(message = "Message is required") //phone number
    private String message;
    
    private String type; // SMS or EMAIL
}

