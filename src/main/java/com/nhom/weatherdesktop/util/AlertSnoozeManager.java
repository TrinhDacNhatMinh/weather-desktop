package com.nhom.weatherdesktop.util;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton manager to handle alert snooze state for stations
 * Keeps track of which stations have alerts temporarily disabled
 */
public class AlertSnoozeManager {
    
    private static AlertSnoozeManager instance;
    private final Map<Long, Instant> snoozeEndTimes;
    
    private AlertSnoozeManager() {
        this.snoozeEndTimes = new ConcurrentHashMap<>();
    }
    
    public static synchronized AlertSnoozeManager getInstance() {
        if (instance == null) {
            instance = new AlertSnoozeManager();
        }
        return instance;
    }
    
    /**
     * Snooze alerts for a specific station
     * @param stationId ID of the station to snooze
     * @param minutes Duration in minutes
     */
    public void snoozeStation(Long stationId, int minutes) {
        Instant endTime = Instant.now().plusSeconds(minutes * 60L);
        snoozeEndTimes.put(stationId, endTime);
    }
    
    /**
     * Check if a station is currently snoozed
     * @param stationId ID of the station to check
     * @return true if alerts should be suppressed, false otherwise
     */
    public boolean isSnoozed(Long stationId) {
        Instant endTime = snoozeEndTimes.get(stationId);
        
        if (endTime == null) {
            return false;
        }
        
        // Check if snooze period has expired
        if (Instant.now().isAfter(endTime)) {
            // Clean up expired entry
            snoozeEndTimes.remove(stationId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Cancel snooze for a station
     * @param stationId ID of the station
     */
    public void cancelSnooze(Long stationId) {
        snoozeEndTimes.remove(stationId);
    }
    
    /**
     * Clear all snooze states
     */
    public void clearAll() {
        snoozeEndTimes.clear();
    }
    
    /**
     * Get remaining snooze time in seconds
     * @param stationId ID of the station
     * @return seconds remaining, or 0 if not snoozed
     */
    public long getRemainingSeconds(Long stationId) {
        Instant endTime = snoozeEndTimes.get(stationId);
        if (endTime == null) {
            return 0;
        }
        
        long remaining = endTime.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
