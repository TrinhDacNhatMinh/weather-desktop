package com.nhom.weatherdesktop.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {
    
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    /**
     * Calculate time ago string from createdAt instant
     * @param createdAt The instant when alert was created
     * @return Formatted string like "5 minutes ago", "2 hours ago", "3 days ago"
     */
    public static String getTimeAgo(Instant createdAt) {
        if (createdAt == null) {
            return "";
        }
        
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        ZonedDateTime created = createdAt.atZone(VIETNAM_ZONE);
        
        Duration duration = Duration.between(created, now);
        
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "just now";
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
