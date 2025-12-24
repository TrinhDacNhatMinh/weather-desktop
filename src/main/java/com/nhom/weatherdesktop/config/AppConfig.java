package com.nhom.weatherdesktop.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    
    private static final Properties PROPS = new Properties();
    
    static {
        try (InputStream input = AppConfig.class
                .getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            PROPS.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    public static String getApiBaseUrl() {
        return PROPS.getProperty("api.base.url", "http://localhost:8080/api");
    }
    
    public static int getApiTimeout() {
        return Integer.parseInt(PROPS.getProperty("api.timeout.seconds", "10"));
    }
    
    public static String getAppTitle() {
        return PROPS.getProperty("app.title", "Weather Desktop");
    }
    
    public static String getAppVersion() {
        return PROPS.getProperty("app.version", "1.0.0");
    }
    
    public static String getProperty(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }
    
    public static String getProperty(String key) {
        return PROPS.getProperty(key);
    }
}
