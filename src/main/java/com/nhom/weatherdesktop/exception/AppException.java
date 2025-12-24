package com.nhom.weatherdesktop.exception;

/**
 * Custom application exception with categorized error types
 * Enables better error handling and user messaging
 * Extends RuntimeException to avoid checked exception boilerplate
 */
public class AppException extends RuntimeException {
    
    private final ErrorType errorType;
    
    public enum ErrorType {
        NETWORK_ERROR("Network connection failed"),
        AUTHENTICATION_ERROR("Authentication required"),
        DATA_NOT_FOUND("Requested data not found"),
        VALIDATION_ERROR("Data validation failed"),
        PERMISSION_ERROR("Permission denied"),
        SERVER_ERROR("Server error occurred"),
        UNKNOWN_ERROR("An unexpected error occurred");
        
        private final String defaultMessage;
        
        ErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    public AppException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public AppException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public AppException(ErrorType errorType) {
        super(errorType.getDefaultMessage());
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
}
