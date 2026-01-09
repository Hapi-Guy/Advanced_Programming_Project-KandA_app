package com.kna.android.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * DateTimeUtils - utility for formatting timestamps
 */
public class DateTimeUtils {
    
    private static final SimpleDateFormat dateFormat = 
        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    private static final SimpleDateFormat timeFormat = 
        new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    private static final SimpleDateFormat fullFormat = 
        new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    
    /**
     * Get relative time string (e.g., "2 hours ago")
     */
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        
        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return dateFormat.format(new Date(timestamp));
        }
    }
    
    /**
     * Format date only
     */
    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
    
    /**
     * Format time only
     */
    public static String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }
    
    /**
     * Format full date and time
     */
    public static String formatFull(long timestamp) {
        return fullFormat.format(new Date(timestamp));
    }
}
