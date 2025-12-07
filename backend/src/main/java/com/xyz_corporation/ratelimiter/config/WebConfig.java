package com.corporation.ratelimiter.config;

import com.corporation.ratelimiter.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:4200,https://rate-limiter-frontend.onrender.com}")
    private String allowedOrigins;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/rate-limits/**", 
                "/api/system/**", 
                "/api/clients/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/v3/api-docs/**"
            );
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Split and trim origins
        String[] origins = allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }
        
        // Build a list that includes all origins plus wildcard for Swagger UI
        // When allowCredentials is true, we can't use "*" but we can use patterns
        String[] allOrigins = new String[origins.length + 1];
        System.arraycopy(origins, 0, allOrigins, 0, origins.length);
        allOrigins[origins.length] = "*"; // Allow all for Swagger UI compatibility
        
        // Apply CORS to all API endpoints - allow all origins for Swagger UI compatibility
        // Swagger UI needs to make requests from the browser, so we need permissive CORS
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*") // Allow all origins - Swagger UI needs this
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .exposedHeaders("X-RateLimit-TimeWindow-Limit", "X-RateLimit-TimeWindow-Remaining",
                           "X-RateLimit-Monthly-Limit", "X-RateLimit-Monthly-Remaining",
                           "X-RateLimit-Global-Limit", "X-RateLimit-Global-Remaining",
                           "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset", "X-RateLimit-Warning")
            .allowCredentials(false) // set to false when using wildcard
            .maxAge(3600); // cache preflight requests for 1 hour
        
        // allow CORS for Swagger UI and API docs
        registry.addMapping("/swagger-ui/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "OPTIONS", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(false);
            
        registry.addMapping("/v3/api-docs/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false);
    }
}

