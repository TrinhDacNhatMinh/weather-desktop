package com.nhom.weatherdesktop.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.exception.AppException;

import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.exception.AppException.ErrorType.*;

/**
 * Centralized HTTP response handler to eliminate duplicate error handling code
 * across service layer
 */
public class ResponseHandler {
    
    /**
     * Handle HTTP response with standard error code mapping for single object
     */
    public static <T> T handle(HttpResponse<String> response, 
                               Class<T> responseType,
                               ObjectMapper mapper) {
        try {
            return switch (response.statusCode()) {
                case 200, 201 -> mapper.readValue(response.body(), responseType);
                case 400 -> throw new AppException(VALIDATION_ERROR, 
                    extractErrorMessage(response, "Invalid request"));
                case 401 -> throw new AppException(AUTHENTICATION_ERROR, 
                    "Unauthorized - Please login again");
                case 403 -> throw new AppException(PERMISSION_ERROR, 
                    extractErrorMessage(response, "Access denied"));
                case 404 -> throw new AppException(DATA_NOT_FOUND, 
                    extractErrorMessage(response, "Resource not found"));
                case 500, 502, 503 -> throw new AppException(SERVER_ERROR, 
                    "Server error (status=" + response.statusCode() + ")");
                default -> throw new AppException(UNKNOWN_ERROR, 
                    "Unexpected error (status=" + response.statusCode() + ")");
            };
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(UNKNOWN_ERROR, "Error parsing response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle HTTP response for generic types (e.g., List, PageResponse)
     */
    public static <T> T handle(HttpResponse<String> response, 
                               TypeReference<T> typeReference,
                               ObjectMapper mapper) {
        try {
            return switch (response.statusCode()) {
                case 200, 201 -> mapper.readValue(response.body(), typeReference);
                case 400 -> throw new AppException(VALIDATION_ERROR, 
                    extractErrorMessage(response, "Invalid request"));
                case 401 -> throw new AppException(AUTHENTICATION_ERROR, 
                    "Unauthorized - Please login again");
                case 403 -> throw new AppException(PERMISSION_ERROR, 
                    extractErrorMessage(response, "Access denied"));
                case 404 -> throw new AppException(DATA_NOT_FOUND, 
                    extractErrorMessage(response, "Resource not found"));
                case 500, 502, 503 -> throw new AppException(SERVER_ERROR, 
                    "Server error (status=" + response.statusCode() + ")");
                default -> throw new AppException(UNKNOWN_ERROR, 
                    "Unexpected error (status=" + response.statusCode() + ")");
            };
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(UNKNOWN_ERROR, "Error parsing response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle HTTP response for void operations (DELETE, etc.)
     */
    public static void handleVoid(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return; // Success
        }
        
        switch (response.statusCode()) {
            case 400 -> throw new AppException(VALIDATION_ERROR, 
                extractErrorMessage(response, "Invalid request"));
            case 401 -> throw new AppException(AUTHENTICATION_ERROR, 
                "Unauthorized - Please login again");
            case 403 -> throw new AppException(PERMISSION_ERROR, 
                extractErrorMessage(response, "Access denied"));
            case 404 -> throw new AppException(DATA_NOT_FOUND, 
                extractErrorMessage(response, "Resource not found"));
            case 500, 502, 503 -> throw new AppException(SERVER_ERROR, 
                "Server error (status=" + response.statusCode() + ")");
            default -> throw new AppException(UNKNOWN_ERROR, 
                "Unexpected error (status=" + response.statusCode() + ")");
        }
    }
    
    /**
     * Extract error message from response body if available
     */
    private static String extractErrorMessage(HttpResponse<String> response, String defaultMessage) {
        try {
            String body = response.body();
            if (body != null && !body.isEmpty()) {
                // Try to extract message from common error response formats
                if (body.contains("\"message\"")) {
                    return body; // Return full body for now, can parse JSON later
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return defaultMessage;
    }
}
