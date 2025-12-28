package com.nhom.weatherdesktop.session;

/**
 * Session context to store user session data
 */
public class SessionContext {
    
    private static String accessToken;
    private static Long selectedStationId;
    private static boolean alertsEnabled = true; // Default: enabled
    
    public static String accessToken() {
        return accessToken;
    }
    
    public static void setAccessToken(String token) {
        accessToken = token;
    }
    
    public static Long selectedStationId() {
        return selectedStationId;
    }
    
    public static void setSelectedStationId(Long stationId) {
        selectedStationId = stationId;
    }
    
    public static boolean areAlertsEnabled() {
        return alertsEnabled;
    }
    
    public static void setAlertsEnabled(boolean enabled) {
        alertsEnabled = enabled;
    }
    
    public static void clear() {
        accessToken = null;
        selectedStationId = null;
        alertsEnabled = true;
    }
}
