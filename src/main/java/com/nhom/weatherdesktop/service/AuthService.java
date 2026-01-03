package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.request.RegisterRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.dto.response.RegisterResponse;
import com.nhom.weatherdesktop.enums.AccessChannel;
import com.nhom.weatherdesktop.service.interfaces.IAuthService;
import com.nhom.weatherdesktop.util.TokenManager;
import com.nhom.weatherdesktop.util.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthService implements IAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
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
            logger.debug("Attempting login for user: {}", request.username());
            
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
                logger.info("Login successful! User: {}, Email: {}", response.name(), response.email());
            }
            
            return response;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP {} error during login: {}", e.getStatusCode(), e.getMessage(), e);
            throw new RuntimeException(getErrorMessage(e), e);
        } catch (IOException e) {
            logger.error("Network error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Network error: Unable to connect to server. Please check your internet connection.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during login: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        try {
            logger.debug("Attempting registration for user: {}", request.username());
            
            RegisterResponse response = httpClient.post(
                "/auth/register",
                request,
                RegisterResponse.class,
                false
            );
            
            logger.info("Registration successful for user: {}", request.username());
            return response;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP {} error during registration: {}", e.getStatusCode(), e.getMessage(), e);
            throw new RuntimeException(getRegistrationErrorMessage(e), e);
        } catch (IOException e) {
            logger.error("Network error during registration: {}", e.getMessage(), e);
            throw new RuntimeException("Network error: Unable to connect to server.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
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

    private String getRegistrationErrorMessage(HttpClientService.HttpException e) {
        return switch (e.getStatusCode()) {
            case 400 -> "Invalid registration details. Please check your input.";
            case 409 -> "Username or email already exists.";
            case 500 -> "Server error. Please try again later.";
            default -> "Registration failed: " + e.getMessage();
        };
    }
}

