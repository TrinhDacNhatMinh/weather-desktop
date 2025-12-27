package com.nhom.weatherdesktop.util;

import java.util.prefs.Preferences;

/**
 * Manages JWT tokens using Java Preferences API for persistence
 */
public class TokenManager {
    private static TokenManager instance;
    private static final String PREFS_NODE = "com.nhom.weatherdesktop.auth";
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    
    private final Preferences prefs;
    private String accessToken;
    private String refreshToken;
    
    private TokenManager() {
        prefs = Preferences.userRoot().node(PREFS_NODE);
        loadTokens();
    }
    
    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }
    
    private void loadTokens() {
        accessToken = prefs.get(ACCESS_TOKEN_KEY, null);
        refreshToken = prefs.get(REFRESH_TOKEN_KEY, null);
    }
    
    public void saveTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        
        if (accessToken != null) {
            prefs.put(ACCESS_TOKEN_KEY, accessToken);
        }
        if (refreshToken != null) {
            prefs.put(REFRESH_TOKEN_KEY, refreshToken);
        }
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isEmpty();
    }
    
    public void clearTokens() {
        accessToken = null;
        refreshToken = null;
        prefs.remove(ACCESS_TOKEN_KEY);
        prefs.remove(REFRESH_TOKEN_KEY);
    }
    
    public String getAuthorizationHeader() {
        return accessToken != null ? "Bearer " + accessToken : null;
    }
}
