package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class ReminderMapActivity extends AppCompatActivity {

    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";

    private TextView reminderDetailsTextView;
    private WebView savedReminderMapWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminder_map);

        View mainView = findViewById(R.id.main);
        int initialLeft = mainView.getPaddingLeft();
        int initialTop = mainView.getPaddingTop();
        int initialRight = mainView.getPaddingRight();
        int initialBottom = mainView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + systemBars.bottom
            );
            return insets;
        });

        reminderDetailsTextView = findViewById(R.id.reminderMapDetailsTextView);
        savedReminderMapWebView = findViewById(R.id.savedReminderMapWebView);
        savedReminderMapWebView.getSettings().setJavaScriptEnabled(true);
        savedReminderMapWebView.getSettings().setDomStorageEnabled(true);

        String reminderId = getIntent().getStringExtra(EXTRA_REMINDER_ID);
        ReminderStorage.Reminder reminder = ReminderStorage.getReminder(this, reminderId);
        if (reminder == null) {
            Toast.makeText(this, "Reminder not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showReminder(reminder);
    }

    private void showReminder(ReminderStorage.Reminder reminder) {
        reminderDetailsTextView.setText(String.format(
                Locale.US,
                "%s\nLatitude: %.6f\nLongitude: %.6f",
                reminder.getTitle(),
                reminder.getLatitude(),
                reminder.getLongitude()
        ));

        String html = String.format(Locale.US,
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css' />" +
                        "<script src='https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>html, body, #map { height: 100%%; margin: 0; padding: 0; }</style>" +
                        "</head>" +
                        "<body>" +
                        "<div id='map'></div>" +
                        "<script>" +
                        "var map = L.map('map').setView([%.6f, %.6f], 16);" +
                        "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {" +
                        "maxZoom: 19, attribution: 'OpenStreetMap, CARTO'}).addTo(map);" +
                        "L.marker([%.6f, %.6f]).addTo(map).bindPopup('%s').openPopup();" +
                        "</script>" +
                        "</body>" +
                        "</html>",
                reminder.getLatitude(),
                reminder.getLongitude(),
                reminder.getLatitude(),
                reminder.getLongitude(),
                reminder.getTitle().replace("'", "\\'"));

        savedReminderMapWebView.loadDataWithBaseURL("https://carto.com/", html, "text/html", "UTF-8", null);
    }
}
