package com.andika.temucashflow.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id"));
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("id"));
    private static final SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id"));
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", new Locale("id"));

    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static String formatFullDate(long timestamp) {
        return fullFormat.format(new Date(timestamp));
    }

    public static String formatDay(long timestamp) {
        return dayFormat.format(new Date(timestamp));
    }

    public static String formatMonth(long timestamp) {
        return monthFormat.format(new Date(timestamp));
    }

    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}
