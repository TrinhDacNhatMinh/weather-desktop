package com.nhom.weatherdesktop.util;

import com.nhom.weatherdesktop.dto.response.AuthResponse;
import com.nhom.weatherdesktop.exception.AuthException;
import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.session.SessionContext;
import javafx.application.Platform;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static com.nhom.weatherdesktop.api.ApiClient.client;

/**
 * Intercepts HTTP responses and automatically refreshes tokens on 401 errors
 */
public class TokenRefreshInterceptor {
    
    private static final AuthService authService = new AuthService();
    private static boolean isRefreshing = false;
    
    /**
     * Sends request with automatic token refresh on 401
     * 
     * @param request The HTTP request to send
     * @return The HTTP response
     * @throws Exception if request fails or token refresh fails
     */
    public static HttpResponse<String> sendWithRefresh(HttpRequest request) throws Exception {
        HttpResponse<String> response = client().send(request, HttpResponse.BodyHandlers.ofString());
        
        // If not 401, return response directly
        if (response.statusCode() != 401) {
            return response;
        }
        
        // 401 detected - attempt token refresh
        synchronized (TokenRefreshInterceptor.class) {
            // Prevent multiple concurrent refresh attempts
            if (isRefreshing) {
                // Wait for ongoing refresh to complete
                Thread.sleep(500);
                // Retry with potentially refreshed token
                return retryRequestWithNewToken(request);
            }
            
            isRefreshing = true;
            
            try {
                // Attempt to refresh token
                String refreshToken = SessionContext.refreshToken();
                
                if (refreshToken == null || refreshToken.isBlank()) {
                    // No refresh token available - redirect to login
                    handleSessionExpired();
                    throw new AuthException("Session expired - please login again");
                }
                
                // Call refresh token API
                AuthResponse authResponse = authService.refreshToken(refreshToken);
                
                // Update session with new tokens
                SessionContext.updateTokens(
                    authResponse.accessToken(), 
                    authResponse.refreshToken()
                );
                
                // Retry original request with new access token
                return retryRequestWithNewToken(request);
                
            } catch (AuthException e) {
                // Refresh token also expired or invalid
                handleSessionExpired();
                throw new AuthException("Session expired - please login again");
                
            } finally {
                isRefreshing = false;
            }
        }
    }
    
    /**
     * Rebuilds and retries the request with the new access token
     */
    private static HttpResponse<String> retryRequestWithNewToken(HttpRequest originalRequest) throws Exception {
        // Rebuild request with new token
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(originalRequest.uri())
                .method(originalRequest.method(), originalRequest.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));
        
        // Copy all headers except Authorization
        originalRequest.headers().map().forEach((key, values) -> {
            if (!"Authorization".equalsIgnoreCase(key)) {
                values.forEach(value -> builder.header(key, value));
            }
        });
        
        // Add new Authorization header
        String newAccessToken = SessionContext.accessToken();
        if (newAccessToken != null && !newAccessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + newAccessToken);
        }
        
        // Send retried request
        HttpRequest newRequest = builder.build();
        return client().send(newRequest, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Handles session expiration by clearing session and redirecting to login
     */
    private static void handleSessionExpired() {
        Platform.runLater(() -> {
            SessionContext.clear();
            AlertService.showError("Your session has expired. Please login again.");
            
            // Navigate to login screen
            try {
                // Get any current window to navigate from
                javafx.stage.Stage stage = (javafx.stage.Stage) 
                    javafx.stage.Stage.getWindows().stream()
                        .filter(javafx.stage.Window::isShowing)
                        .findFirst()
                        .orElse(null);
                
                if (stage != null) {
                    NavigationService.navigateToLogin(stage.getScene().getRoot());
                }
            } catch (Exception e) {
                // Navigation error is silently handled
            }
        });
    }
}
