package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhom.weatherdesktop.util.AppConfig;
import com.nhom.weatherdesktop.util.TokenManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Client Service for making REST API calls
 */
public class HttpClientService {
    private static HttpClientService instance;
    private final ObjectMapper objectMapper;
    private final AppConfig config;
    private final TokenManager tokenManager;
    
    private HttpClientService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.config = AppConfig.getInstance();
        this.tokenManager = TokenManager.getInstance();
    }
    
    public static HttpClientService getInstance() {
        if (instance == null) {
            instance = new HttpClientService();
        }
        return instance;
    }
    
    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType) throws IOException, HttpException {
        return post(endpoint, requestBody, responseType, false);
    }
    
    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType, boolean includeAuth) 
            throws IOException, HttpException {
        
        String fullUrl = config.getApiBaseUrl() + endpoint;
        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(config.getApiTimeout() * 1000);
            connection.setReadTimeout(config.getApiTimeout() * 1000);
            
            if (includeAuth && tokenManager.isAuthenticated()) {
                connection.setRequestProperty("Authorization", tokenManager.getAuthorizationHeader());
            }
            
            if (requestBody != null) {
                String jsonBody = objectMapper.writeValueAsString(requestBody);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode >= 200 && responseCode < 300) {
                if (responseType != null && responseType != Void.class) {
                    return objectMapper.readValue(connection.getInputStream(), responseType);
                }
                return null;
            } else if (responseCode == 401) {
                throw new HttpException(401, "Unauthorized - Invalid credentials");
            } else if (responseCode == 403) {
                throw new HttpException(403, "Forbidden - Access denied");
            } else if (responseCode >= 500) {
                throw new HttpException(responseCode, "Server error - Please try again later");
            } else {
                String errorMessage = "HTTP Error " + responseCode;
                try {
                    String errorBody = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    errorMessage = errorBody;
                } catch (Exception ignored) {
                }
                throw new HttpException(responseCode, errorMessage);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    public static class HttpException extends Exception {
        private final int statusCode;
        
        public HttpException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}
