package com.nhom.weatherdesktop.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {
    
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    /**
     * Calculate time ago string from createdAt instant
     * 
     * @param createdAt The instant when alert was created (UTC timestamp)
     * @return Formatted string like "just now", "30 seconds ago", "5 minutes ago", "2 hours ago", "3 days ago"
     */
    public static String getTimeAgo(Instant createdAt) {
        if (createdAt == null) {
            return "";
        }
        
        // Get current time as Instant (UTC)
        Instant now = Instant.now();
        
        // Calculate duration between created time and now (both in UTC)
        Duration duration = Duration.between(createdAt, now);
        
        long seconds = duration.getSeconds();
        
        // Handle negative values (future dates - shouldn't happen but just in case)
        if (seconds < 0) {
            return "just now";
        }
        
        if (seconds < 10) {
            return "just now";
        } else if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = seconds / 31536000;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
}
