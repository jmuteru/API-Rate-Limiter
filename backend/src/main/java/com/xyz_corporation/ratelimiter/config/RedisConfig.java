package com.corporation.ratelimiter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Slf4j
public class RedisConfig {
    
    @Value("${spring.data.redis.url:}")
    private String redisUrl;
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        
        // Parse REDIS_URL if provided (for Render deployment)
        // Check if URL is provided and not just the placeholder/default value
        boolean hasRedisUrl = redisUrl != null 
            && !redisUrl.isEmpty() 
            && !redisUrl.equals("${REDIS_URL:}")
            && (redisUrl.startsWith("redis://") || redisUrl.startsWith("rediss://"));
        
        if (hasRedisUrl) {
            try {
                URI uri = new URI(redisUrl);
                
                // Extract host and port
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 6379 : uri.getPort();
                
                // Extract password from userInfo (format: redis://username:password@host:port)
                String userInfo = uri.getUserInfo();
                String password = null;
                if (userInfo != null && userInfo.contains(":")) {
                    password = userInfo.split(":", 2)[1];
                } else if (userInfo != null && !userInfo.isEmpty()) {
                    // Sometimes password is provided without username
                    password = userInfo;
                }
                
                // If password is still empty, use the explicit password property
                if ((password == null || password.isEmpty()) && redisPassword != null && !redisPassword.isEmpty()) {
                    password = redisPassword;
                }
                
                config.setHostName(host);
                config.setPort(port);
                if (password != null && !password.isEmpty()) {
                    config.setPassword(password);
                }
                
                log.info("Configured Redis from URL: {}:{} (password: {})", host, port, password != null ? "***" : "none");
            } catch (URISyntaxException e) {
                log.error("Invalid REDIS_URL format: {}. Falling back to individual properties.", redisUrl, e);
                // Fall back to individual properties
                config.setHostName(redisHost);
                config.setPort(redisPort);
                if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                }
            }
        } else {
            // Use individual properties
            config.setHostName(redisHost);
            config.setPort(redisPort);
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }
            log.info("Configured Redis from properties: {}:{} (password: {})", redisHost, redisPort, redisPassword != null && !redisPassword.isEmpty() ? "***" : "none");
        }
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setPoolConfig(poolConfig);
        
        // Initialize the factory (doesn't actually connect yet)
        try {
            factory.afterPropertiesSet();
            log.info("Redis connection factory configured successfully for {}:{}", config.getHostName(), config.getPort());
        } catch (Exception e) {
            log.warn("Redis connection factory initialization warning: {}. Connection will be attempted on first use.", e.getMessage());
        }
        
        return factory;
    }
    
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}

