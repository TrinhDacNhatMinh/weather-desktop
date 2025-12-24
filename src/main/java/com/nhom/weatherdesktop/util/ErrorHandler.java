package com.nhom.weatherdesktop.util;

import com.nhom.weatherdesktop.exception.AppException;

/**
 * Centralized error handler for consistent error messaging
 */
public class ErrorHandler {
    
    /**
     * Get user-friendly error message from exception
     */
    public static String getUserMessage(Exception e) {
        if (e instanceof AppException) {
            AppException appEx = (AppException) e;
            return formatMessage(appEx.getErrorType(), e.getMessage());
        }
        
        // For RuntimeException, try to extract meaningful message
        if (e instanceof RuntimeException) {
            String message = e.getMessage();
            if (message != null) {
                // Extract common error patterns
                if (message.contains("Unauthorized")) {
                    return "Session expired. Please login again.";
                }
                if (message.contains("Forbidden") || message.contains("Permission")) {
                    return "You don't have permission for this operation.";
                }
                if (message.contains("not found") || message.contains("404")) {
                    return "Requested data not found.";
                }
                if (message.contains("Server error") || message.contains("500")) {
                    return "Server error. Please try again later.";
                }
                if (message.contains("Network") || message.contains("Connection")) {
                    return "Network connection error. Check internet connection.";
                }
                
                // Return original message if no pattern matched
                return message;
            }
        }
        
        return "An error occurred: " + e.getClass().getSimpleName();
    }
    
    /**
     * Format error message based on type
     */
    private static String formatMessage(AppException.ErrorType errorType, String details) {
       return switch (errorType) {
            case NETWORK_ERROR -> "Network connection error: " + details;
            case AUTHENTICATION_ERROR -> "Authentication error: " + details;
            case DATA_NOT_FOUND -> "Not found: " + details;
            case VALIDATION_ERROR -> "Invalid data: " + details;
            case PERMISSION_ERROR -> "Permission denied: " + details;
            case SERVER_ERROR -> "Server error: " + details;
            case UNKNOWN_ERROR -> "Unknown error: " + details;
        };
    }
    
    /**
     * Extract HTTP status code from error message if present
     */
    public static Integer extractStatusCode(String message) {
        if (message == null) return null;
        
        // Try to extract status code like "status=401" or "(401)"
        String[] patterns = {"status=", "\\("};
        for (String pattern : patterns) {
            int index = message.indexOf(pattern);
            if (index != -1) {
                String after = message.substring(index + pattern.length());
                StringBuilder code = new StringBuilder();
                for (char c : after.toCharArray()) {
                    if (Character.isDigit(c)) {
                        code.append(c);
                    } else {
                        break;
                    }
                }
                if (code.length() == 3) {
                    try {
                        return Integer.parseInt(code.toString());
                    } catch (NumberFormatException e) {
                        // Continue to next pattern
                    }
                }
            }
        }
        return null;
    }
}
