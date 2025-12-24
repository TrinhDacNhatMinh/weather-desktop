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
                    return "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
                }
                if (message.contains("Forbidden") || message.contains("Permission")) {
                    return "Bạn không có quyền thực hiện thao tác này.";
                }
                if (message.contains("not found") || message.contains("404")) {
                    return "Không tìm thấy dữ liệu yêu cầu.";
                }
                if (message.contains("Server error") || message.contains("500")) {
                    return "Lỗi máy chủ. Vui lòng thử lại sau.";
                }
                if (message.contains("Network") || message.contains("Connection")) {
                    return "Lỗi kết nối mạng. Kiểm tra kết nối internet.";
                }
                
                // Return original message if no pattern matched
                return message;
            }
        }
        
        return "Đã xảy ra lỗi: " + e.getClass().getSimpleName();
    }
    
    /**
     * Format error message based on type
     */
    private static String formatMessage(AppException.ErrorType errorType, String details) {
       return switch (errorType) {
            case NETWORK_ERROR -> "Lỗi kết nối mạng: " + details;
            case AUTHENTICATION_ERROR -> "Lỗi xác thực: " + details;
            case DATA_NOT_FOUND -> "Không tìm thấy: " + details;
            case VALIDATION_ERROR -> "Dữ liệu không hợp lệ: " + details;
            case PERMISSION_ERROR -> "Không có quyền: " + details;
            case SERVER_ERROR -> "Lỗi máy chủ: " + details;
            case UNKNOWN_ERROR -> "Lỗi không xác định: " + details;
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
