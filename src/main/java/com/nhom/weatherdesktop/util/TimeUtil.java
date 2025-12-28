package com.nhom.weatherdesktop.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {
    
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    /**
     * Calculate time ago string from createdAt instant
     * NOTE: The API returns timestamps in VN timezone but labels them as UTC (with Z suffix)
     * We need to interpret the timestamp as VN time, not UTC
     * 
     * @param createdAt The instant when alert was created (mislabeled as UTC, actually VN time)
     * @return Formatted string like "just now", "30 seconds ago", "5 minutes ago", "2 hours ago", "3 days ago"
     */
    public static String getTimeAgo(Instant createdAt) {
        if (createdAt == null) {
            return "";
        }
        
        // API bug: createdAt is labeled as UTC but is actually VN time
        // We need to convert it properly:
        // 1. Get the timestamp as if it were VN time (not UTC)
        ZonedDateTime createdVN = createdAt.atZone(ZoneId.of("UTC")).withZoneSameLocal(VIETNAM_ZONE);
        
        // 2. Get current VN time
        ZonedDateTime nowVN = ZonedDateTime.now(VIETNAM_ZONE);
        
        // 3. Calculate duration
        Duration duration = Duration.between(createdVN, nowVN);
        
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
