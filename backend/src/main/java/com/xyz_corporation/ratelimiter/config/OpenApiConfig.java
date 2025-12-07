package com.corporation.ratelimiter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${SERVER_URL:}")
    private String serverUrl;

    private final Environment environment;

    public OpenApiConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        
        // check if we are  in prod...
        boolean isProduction = environment.acceptsProfiles("prod") || 
                              System.getenv("RENDER") != null ||
                              System.getenv("RENDER_SERVICE_NAME") != null;
        
        // in production, prioritize production server and don't include localhost...
        if (isProduction) {
            // use SERVER_URL if provided, otherwise try to construct from RENDER_URL
            String productionUrl = serverUrl;
            if ((productionUrl == null || productionUrl.isEmpty() || productionUrl.equals("${SERVER_URL:}")) 
                && System.getenv("RENDER_EXTERNAL_URL") != null) {
                productionUrl = System.getenv("RENDER_EXTERNAL_URL");
            }
            if ((productionUrl == null || productionUrl.isEmpty() || productionUrl.equals("${SERVER_URL:}")) 
                && System.getenv("RENDER_SERVICE_URL") != null) {
                productionUrl = System.getenv("RENDER_SERVICE_URL");
            }
            if (productionUrl == null || productionUrl.isEmpty() || productionUrl.equals("${SERVER_URL:}")) {
                // fallback to default Render URL pattern
                productionUrl = "https://rate-limiter-backend.onrender.com";
            }
            
            // add production server as primary
            servers.add(new Server()
                .url(productionUrl)
                .description("Production server"));
        } else {
            // in dev, include localhost as primary
            servers.add(new Server()
                .url("http://localhost:8080")
                .description("Local development server"));
        }
        
        return new OpenAPI()
            .info(new Info()
                .title("API Rate Limiter Service")
                .version("1.0.0")
                .description("Rate limiting service for notification API with support for time window, monthly, and global rate limits")
                .contact(new Contact()
                    .name("Corporation X,Y,Z")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(servers);
    }
}



