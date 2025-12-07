package com.corporation.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@Slf4j
public class RateLimiterApplication {
    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        loadEnvFile();
        
        SpringApplication.run(RateLimiterApplication.class, args);
    }
    
  
    private static void loadEnvFile() {
        try {
            //  find .env  in backend dir ...
            File envFile = new File(".env");
            
            // if not found in , try parent directory 
            if (!envFile.exists()) {
                envFile = new File("backend/.env");
            }
            
            // still not found, try in the project root dir
            if (!envFile.exists()) {
                envFile = new File("../.env");
            }
            
            if (envFile.exists() && envFile.isFile()) {
                log.info("Loading environment variables from: {}", envFile.getAbsolutePath());
                try (Scanner scanner = new Scanner(envFile)) {
                    int loadedCount = 0;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        
                        // skip empty lines and comments
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        
                        // parse key = value pairs
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            String key = line.substring(0, equalsIndex).trim();
                            String value = line.substring(equalsIndex + 1).trim();
                            
                            // remove quotes if present
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            } else if (value.startsWith("'") && value.endsWith("'")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            
                            
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                                loadedCount++;
                            }
                        }
                    }
                    log.info("Loaded {} environment variables from .env file", loadedCount);
                }
            }
        } catch (FileNotFoundException e) {
            // no .env file found, using system environment variables only
        } catch (Exception e) {
            log.warn("Error loading .env file: {}. Continuing with system environment variables only.", e.getMessage());
        }
    }
}

