package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.enums.AccessChannel;
import com.nhom.weatherdesktop.service.interfaces.IAuthService;
import com.nhom.weatherdesktop.util.TokenManager;
import com.nhom.weatherdesktop.util.UserSession;

import java.io.IOException;

public class AuthService implements IAuthService {
    
    private final HttpClientService httpClient;
    private final TokenManager tokenManager;
    private final UserSession userSession;
    
    public AuthService() {
        this.httpClient = HttpClientService.getInstance();
        this.tokenManager = TokenManager.getInstance();
        this.userSession = UserSession.getInstance();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            LoginRequest finalRequest = request;
            if (request.accessChannel() == null) {
                finalRequest = new LoginRequest(
                    request.username(),
                    request.password(),
                    AccessChannel.DESKTOP
                );
            }
            
            LoginResponse response = httpClient.post(
                "/auth/login",
                finalRequest,
                LoginResponse.class,
                false
            );
            
            if (response != null) {
                tokenManager.saveTokens(response.accessToken(), response.refreshToken());
                userSession.setUserInfo(response.name(), response.email());
            }
            
            return response;
            
        } catch (HttpClientService.HttpException e) {
            throw new RuntimeException(getErrorMessage(e), e);
        } catch (IOException e) {
            throw new RuntimeException("Network error: Unable to connect to server. Please check your internet connection.", e);
        }
    }
    
    public void logout() {
        tokenManager.clearTokens();
        userSession.clear();
    }
    
    public boolean isLoggedIn() {
        return tokenManager.isAuthenticated() && userSession.isActive();
    }
    
    private String getErrorMessage(HttpClientService.HttpException e) {
        return switch (e.getStatusCode()) {
            case 401 -> "Invalid username or password. Please try again.";
            case 403 -> "Access denied. Your account may be inactive or you don't have permission to login from this device.";
            case 500 -> "Server error. Please try again later.";
            default -> "Login failed: " + e.getMessage();
        };
    }
}

