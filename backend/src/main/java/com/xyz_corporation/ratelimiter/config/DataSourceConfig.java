package com.corporation.ratelimiter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Slf4j
public class DataSourceConfig {
    
    @Value("${DATABASE_URL:}")
    private String databaseUrl;
    
    @Value("${DB_USERNAME:${DATABASE_USERNAME:}}")
    private String dbUsername;
    
    @Value("${DB_PASSWORD:${DATABASE_PASSWORD:}}")
    private String dbPassword;
    
    @Bean
    @Primary
    public DataSource dataSource() {
        // If DATABASE_URL is not set or empty, use H2 in-memory
        if (databaseUrl == null || databaseUrl.isEmpty() || databaseUrl.equals("${DATABASE_URL:}")) {
            log.info("No DATABASE_URL provided, using H2 in-memory database");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:ratelimiterdb");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            return new HikariDataSource(config);
        }
        
        // Parse DATABASE_URL
        try {
            // Handle both postgresql:// and jdbc:postgresql:// formats
            String url = databaseUrl.trim();
            String urlWithoutJdbc = url;
            
            if (url.startsWith("jdbc:")) {
                urlWithoutJdbc = url.substring(5); // Remove "jdbc:"
            } else if (url.startsWith("postgresql://")) {
                // Already in postgresql:// format
            } else if (url.startsWith("postgres://")) {
                urlWithoutJdbc = "postgresql://" + url.substring("postgres://".length());
            }
            
            // Parse the URL to extract components
            // Handle the case where username:password@host format might not parse correctly
            URI uri;
            try {
                uri = new URI(urlWithoutJdbc);
            } catch (URISyntaxException e) {
                // If URI parsing fails, try manual parsing for common formats
                log.warn("URI parsing failed, attempting manual parsing: {}", e.getMessage());
                return parseDatabaseUrlManually(url);
            }
            
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            String database = path != null && path.startsWith("/") ? path.substring(1) : (path != null ? path : "");
            
            // Extract username and password from userInfo or use separate env vars
            String username = (dbUsername != null && !dbUsername.isEmpty()) ? dbUsername : null;
            String password = (dbPassword != null && !dbPassword.isEmpty()) ? dbPassword : null;
            
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                if (userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    if (username == null || username.isEmpty()) {
                        username = parts[0];
                    }
                    if (password == null || password.isEmpty()) {
                        password = parts[1];
                    }
                } else {
                    if (username == null || username.isEmpty()) {
                        username = userInfo;
                    }
                }
            }
            
            // Validate we have required components
            if (host == null || host.isEmpty()) {
                throw new URISyntaxException(url, "Host cannot be determined from URL");
            }
            if (database == null || database.isEmpty()) {
                throw new URISyntaxException(url, "Database name cannot be determined from URL");
            }
            
            // Build proper JDBC URL (credentials as query parameters, not in host)
            String jdbcUrl;
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s",
                    host, port, database, username, password);
            } else if (username != null && !username.isEmpty()) {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?user=%s",
                    host, port, database, username);
            } else {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                    host, port, database);
            }
            
            log.info("Configuring PostgreSQL datasource: {}:{}/{} (user: {})", 
                host, port, database, username != null ? username : "none");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.postgresql.Driver");
            if (username != null && !username.isEmpty()) {
                config.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            
            return new HikariDataSource(config);
            
        } catch (URISyntaxException e) {
            log.error("Invalid DATABASE_URL format: {}. Falling back to H2.", databaseUrl, e);
            return createH2DataSource();
        } catch (Exception e) {
            log.error("Error configuring datasource: {}. Falling back to H2.", e.getMessage(), e);
            return createH2DataSource();
        }
    }
    
    /**
     * Manual parsing for URLs that don't parse correctly as URI
     */
    private DataSource parseDatabaseUrlManually(String url) {
        log.info("Attempting manual parsing of DATABASE_URL");
        
        // Pattern: jdbc:postgresql://username:password@host:port/database
        // or: postgresql://username:password@host:port/database
        
        String urlToParse = url;
        if (urlToParse.startsWith("jdbc:")) {
            urlToParse = urlToParse.substring(5);
        }
        
        if (!urlToParse.startsWith("postgresql://")) {
            log.warn("Unrecognized database URL format, falling back to H2");
            return createH2DataSource();
        }
        
        try {
            // Remove postgresql:// prefix
            String rest = urlToParse.substring("postgresql://".length());
            
            // Find @ symbol to separate credentials from host
            int atIndex = rest.indexOf('@');
            String credentialsPart = "";
            String hostPart = rest;
            
            if (atIndex > 0) {
                credentialsPart = rest.substring(0, atIndex);
                hostPart = rest.substring(atIndex + 1);
            }
            
            // Parse credentials
            String username = (dbUsername != null && !dbUsername.isEmpty()) ? dbUsername : null;
            String password = (dbPassword != null && !dbPassword.isEmpty()) ? dbPassword : null;
            
            if (!credentialsPart.isEmpty()) {
                if (credentialsPart.contains(":")) {
                    String[] creds = credentialsPart.split(":", 2);
                    if (username == null || username.isEmpty()) {
                        username = creds[0];
                    }
                    if (password == null || password.isEmpty()) {
                        password = creds[1];
                    }
                } else {
                    if (username == null || username.isEmpty()) {
                        username = credentialsPart;
                    }
                }
            }
            
            // Parse host:port/database
            int slashIndex = hostPart.indexOf('/');
            String hostPort = slashIndex > 0 ? hostPart.substring(0, slashIndex) : hostPart;
            String database = slashIndex > 0 ? hostPart.substring(slashIndex + 1) : "";
            
            // Parse host and port
            String host;
            int port = 5432;
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":", 2);
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("Invalid port in URL, using default 5432");
                }
            } else {
                host = hostPort;
            }
            
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("Host cannot be determined");
            }
            if (database == null || database.isEmpty()) {
                throw new IllegalArgumentException("Database name cannot be determined");
            }
            
            // Build JDBC URL
            String jdbcUrl;
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s",
                    host, port, database, username, password);
            } else if (username != null && !username.isEmpty()) {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?user=%s",
                    host, port, database, username);
            } else {
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                    host, port, database);
            }
            
            log.info("Manually parsed PostgreSQL URL: {}:{}/{} (user: {})", 
                host, port, database, username != null ? username : "none");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.postgresql.Driver");
            if (username != null && !username.isEmpty()) {
                config.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            
            return new HikariDataSource(config);
            
        } catch (Exception e) {
            log.error("Manual parsing failed: {}. Falling back to H2.", e.getMessage(), e);
            return createH2DataSource();
        }
    }
    
    private DataSource createH2DataSource() {
        log.info("Creating H2 in-memory datasource");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:ratelimiterdb");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }
}

