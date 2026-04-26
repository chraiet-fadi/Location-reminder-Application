package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class ReminderStorage {

    private static final String PREFS_NAME = "location_reminder_prefs";
    private static final String KEY_TITLE = "reminder_title";
    private static final String KEY_LATITUDE = "reminder_latitude";
    private static final String KEY_LONGITUDE = "reminder_longitude";
    private static final String KEY_HAS_REMINDER = "has_reminder";

    private ReminderStorage() {
    }

    public static void saveReminder(Context context, String title, double latitude, double longitude) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_HAS_REMINDER, true)
                .putString(KEY_TITLE, title)
                .putFloat(KEY_LATITUDE, (float) latitude)
                .putFloat(KEY_LONGITUDE, (float) longitude)
                .apply();
    }

    public static boolean hasReminder(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_HAS_REMINDER, false);
    }

    public static String getTitle(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_TITLE, "");
    }

    public static double getLatitude(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getFloat(KEY_LATITUDE, 0f);
    }

    public static double getLongitude(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getFloat(KEY_LONGITUDE, 0f);
    }
}
