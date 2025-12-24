package com.nhom.weatherdesktop.config;

/**
 * UI-related constants to avoid magic numbers scattered throughout the codebase
 */
public class UIConstants {
    
    // Sidebar dimensions
    public static final double SIDEBAR_EXPANDED_WIDTH = 220.0;
    public static final double SIDEBAR_COLLAPSED_WIDTH = 60.0;
    
    // WebSocket connection retry
    public static final int WEBSOCKET_RETRY_COUNT = 30;
    public static final int WEBSOCKET_RETRY_DELAY_MS = 100;
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_ALERTS_PAGE_SIZE = 100;
    
    private UIConstants() {
        // Prevent instantiation
    }
}
