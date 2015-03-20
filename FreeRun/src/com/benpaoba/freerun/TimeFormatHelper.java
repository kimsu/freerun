package com.benpaoba.freerun;

import android.graphics.Color;

public abstract class TimeFormatHelper {

    public static int determineColor(int seconds, int warningTime) {
        if (seconds <= 0) {
            return Color.RED;
        } else if (seconds <= warningTime) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }
    
    public static String formatTime(int seconds) {
    	int hour = seconds / 3600;
    	int minutes = (seconds - hour * 3600) / 60;
    	int lastSeconds = (seconds - hour * 3600) % 60;
        return padWithZeros(hour) + ":" + padWithZeros(minutes) + ":" + padWithZeros(lastSeconds);
    }

    public static String formatTime(float timeValue) {
        return formatTime((int) timeValue);
    }

    private static String padWithZeros(long value) {
        return value < 10 ? "0" + value : Long.toString(value);
    }
}
