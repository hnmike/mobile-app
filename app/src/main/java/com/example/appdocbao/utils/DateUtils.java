package com.example.appdocbao.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    // Format date as relative time (e.g., "2 hours ago")
    public static String getRelativeTimeSpan(Date date) {
        if (date == null) {
            return "";
        }
        
        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        
        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            return formatDate(date);
        }
    }
    
    // Get time ago format (used in articles list)
    public static String getTimeAgo(Date date) {
        if (date == null) {
            return "";
        }
        
        return getRelativeTimeSpan(date);
    }
    
    // Format date as "dd/MM/yyyy"
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }
    
    // Format time as "HH:mm"
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return TIME_FORMAT.format(date);
    }
    
    // Format date and time as "dd/MM/yyyy HH:mm"
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATETIME_FORMAT.format(date);
    }
} 