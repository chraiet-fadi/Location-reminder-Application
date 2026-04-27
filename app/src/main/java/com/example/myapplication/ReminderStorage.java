package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReminderStorage {

    private static final String PREFS_NAME = "location_reminder_prefs";
    private static final String KEY_TITLE = "reminder_title";
    private static final String KEY_LATITUDE = "reminder_latitude";
    private static final String KEY_LONGITUDE = "reminder_longitude";
    private static final String KEY_HAS_REMINDER = "has_reminder";
    private static final String KEY_REMINDERS = "reminders";
    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";

    private ReminderStorage() {
    }

    public static void saveReminder(Context context, String title, double latitude, double longitude) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<Reminder> reminders = getReminders(context);
        reminders.add(new Reminder(UUID.randomUUID().toString(), title, latitude, longitude));
        preferences.edit()
                .putString(KEY_REMINDERS, remindersToJson(reminders).toString())
                .remove(KEY_HAS_REMINDER)
                .remove(KEY_TITLE)
                .remove(KEY_LATITUDE)
                .remove(KEY_LONGITUDE)
                .apply();
    }

    public static boolean hasReminder(Context context) {
        return !getReminders(context).isEmpty();
    }

    public static List<Reminder> getReminders(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String remindersJson = preferences.getString(KEY_REMINDERS, null);

        if (remindersJson == null && preferences.getBoolean(KEY_HAS_REMINDER, false)) {
            List<Reminder> migratedReminders = new ArrayList<>();
            migratedReminders.add(new Reminder(
                    UUID.randomUUID().toString(),
                    preferences.getString(KEY_TITLE, ""),
                    preferences.getFloat(KEY_LATITUDE, 0f),
                    preferences.getFloat(KEY_LONGITUDE, 0f)
            ));
            preferences.edit()
                    .putString(KEY_REMINDERS, remindersToJson(migratedReminders).toString())
                    .remove(KEY_HAS_REMINDER)
                    .remove(KEY_TITLE)
                    .remove(KEY_LATITUDE)
                    .remove(KEY_LONGITUDE)
                    .apply();
            return migratedReminders;
        }

        List<Reminder> reminders = new ArrayList<>();
        if (remindersJson == null || remindersJson.isEmpty()) {
            return reminders;
        }

        try {
            JSONArray array = new JSONArray(remindersJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject reminderObject = array.getJSONObject(i);
                reminders.add(new Reminder(
                        reminderObject.getString(JSON_ID),
                        reminderObject.getString(JSON_TITLE),
                        reminderObject.getDouble(JSON_LATITUDE),
                        reminderObject.getDouble(JSON_LONGITUDE)
                ));
            }
        } catch (JSONException e) {
            preferences.edit().remove(KEY_REMINDERS).apply();
        }

        return reminders;
    }

    public static Reminder getReminder(Context context, String reminderId) {
        for (Reminder reminder : getReminders(context)) {
            if (reminder.getId().equals(reminderId)) {
                return reminder;
            }
        }

        return null;
    }

    public static String getTitle(Context context) {
        List<Reminder> reminders = getReminders(context);
        return reminders.isEmpty() ? "" : reminders.get(0).getTitle();
    }

    public static double getLatitude(Context context) {
        List<Reminder> reminders = getReminders(context);
        return reminders.isEmpty() ? 0 : reminders.get(0).getLatitude();
    }

    public static double getLongitude(Context context) {
        List<Reminder> reminders = getReminders(context);
        return reminders.isEmpty() ? 0 : reminders.get(0).getLongitude();
    }

    private static JSONArray remindersToJson(List<Reminder> reminders) {
        JSONArray array = new JSONArray();

        for (Reminder reminder : reminders) {
            JSONObject reminderObject = new JSONObject();
            try {
                reminderObject.put(JSON_ID, reminder.getId());
                reminderObject.put(JSON_TITLE, reminder.getTitle());
                reminderObject.put(JSON_LATITUDE, reminder.getLatitude());
                reminderObject.put(JSON_LONGITUDE, reminder.getLongitude());
                array.put(reminderObject);
            } catch (JSONException ignored) {
            }
        }

        return array;
    }

    public static class Reminder {
        private final String id;
        private final String title;
        private final double latitude;
        private final double longitude;

        Reminder(String id, String title, double latitude, double longitude) {
            this.id = id;
            this.title = title;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
