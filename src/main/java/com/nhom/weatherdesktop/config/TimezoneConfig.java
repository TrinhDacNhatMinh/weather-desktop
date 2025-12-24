package com.nhom.weatherdesktop.config;

/**
 * Timezone configuration to handle server/client timezone mismatches
 */
public class TimezoneConfig {
    
    /**
     * Server timezone offset from UTC in hours
     * WORKAROUND: Server returns timestamp in +07:00 timezone but marks it as UTC (Z)
     * We need to subtract this offset to get actual UTC time
     */
    public static final int SERVER_TIMEZONE_OFFSET_HOURS = 7;
    
    /**
     * Get timezone offset in seconds for easier calculations
     */
    public static int getServerTimezoneOffsetSeconds() {
        return SERVER_TIMEZONE_OFFSET_HOURS * 3600;
    }
    
    private TimezoneConfig() {
        // Prevent instantiation
    }
}
