package com.example.appdocbao.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    /**
     * Returns a Vietnamese relative time string indicating how long ago the given date occurred.
     *
     * If the date is within the last minute, returns "Vừa xong". For times within the last hour, returns the number of minutes ago; within the last day, the number of hours ago; within the last week, the number of days ago. For dates older than a week, returns the formatted date as "dd/MM/yyyy". Returns an empty string if the input is null.
     *
     * @param date the date to compare to the current time
     * @return a Vietnamese relative time string or a formatted date string if older than a week; empty string if date is null
     */
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
    
    /****
     * Returns a human-readable English string representing how long ago the given date occurred.
     *
     * The result expresses the elapsed time in minutes, hours, days, weeks, months, or years, depending on the difference between the current time and the provided date.
     * Returns an empty string if the input date is null.
     *
     * @param date the date to compare with the current time
     * @return a relative time string such as "5 minutes ago", "2 hours ago", "3 days ago", "1 week ago", "4 months ago", or "2 years ago"
     */
    public static String getTimeAgo(Date date) {
        if (date == null) {
            return "";
        }
        
        long currentTime = new Date().getTime();
        long dateTime = date.getTime();
        long timeDiff = currentTime - dateTime;
        
        // Convert milliseconds to minutes, hours, days
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long diffHours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long diffDays = TimeUnit.MILLISECONDS.toDays(timeDiff);
        
        if (diffMinutes < 60) {
            return diffMinutes + " minutes ago";
        } else if (diffHours < 24) {
            return diffHours + " hours ago";
        } else if (diffDays < 7) {
            return diffDays + " days ago";
        } else if (diffDays < 30) {
            long weeks = diffDays / 7;
            return weeks + " weeks ago";
        } else if (diffDays < 365) {
            long months = diffDays / 30;
            return months + " months ago";
        } else {
            long years = diffDays / 365;
            return years + " years ago";
        }
    }
    
    /****
     * Formats the given date as "dd/MM/yyyy".
     *
     * @param date the date to format; if null, returns an empty string
     * @return the formatted date string, or an empty string if the date is null
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }
    
    /****
     * Formats the given date as a time string in "HH:mm" format.
     *
     * @param date the date to format; if null, returns an empty string
     * @return the formatted time string, or an empty string if the date is null
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return TIME_FORMAT.format(date);
    }
    
    /****
     * Formats the given date and time as "dd/MM/yyyy HH:mm".
     *
     * @param date the date to format; if null, returns an empty string
     * @return the formatted date and time string, or an empty string if the date is null
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATETIME_FORMAT.format(date);
    }
} 