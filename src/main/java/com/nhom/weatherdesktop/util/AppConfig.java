package com.nhom.weatherdesktop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration manager
 * Loads and provides access to application.properties
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance;
    private final Properties properties;
    
    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }
    
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("Unable to find application.properties");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Error loading application.properties: {}", e.getMessage(), e);
        }
    }
    
    public String getApiBaseUrl() {
        return properties.getProperty("api.base.url", "http://localhost:8080/api");
    }
    
    public int getApiTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout.seconds", "10"));
    }
    
    public String getWebSocketUrl() {
        return properties.getProperty("websocket.url", "ws://localhost:8080/ws");
    }
    
    public String getAppTitle() {
        return properties.getProperty("app.title", "Weather Desktop");
    }
    
    public String getAppVersion() {
        return properties.getProperty("app.version", "1.0.0");
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
