package com.nhom.weatherdesktop.session;

import com.nhom.weatherdesktop.dto.response.LoginResponse;

public class SessionContext {
    
    private static String accessToken;
    private static String refreshToken;
    private static String userName;
    private static String userEmail;
    private static Long selectedStationId; // Remember last selected station
    
    public static void set(LoginResponse response) {
        accessToken = response.accessToken();
        refreshToken = response.refreshToken();
        userName = response.name();
        userEmail = response.email();
    }
    
    public static void updateTokens(String newAccessToken, String newRefreshToken) {
        accessToken = newAccessToken;
        refreshToken = newRefreshToken;
    }
    
    public static String accessToken() {
        return accessToken;
    }
    
    public static String refreshToken() {
        return refreshToken;
    }
    
    public static String userName() {
        return userName;
    }
    
    public static String userEmail() {
        return userEmail;
    }
    
    public static Long selectedStationId() {
        return selectedStationId;
    }
    
    public static void setSelectedStationId(Long stationId) {
        selectedStationId = stationId;
    }
    
    public static void clear() {
        accessToken = null;
        refreshToken = null;
        userName = null;
        userEmail = null;
        selectedStationId = null;
    }
    
    public static boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }
}
