package com.corporation.ratelimiter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerController {
    
    @GetMapping("/swagger-ui.html")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}


