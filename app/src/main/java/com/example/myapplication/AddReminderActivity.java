package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4001;
    private static final double DEFAULT_LATITUDE = 34.8504;
    private static final double DEFAULT_LONGITUDE = 5.7281;

    private EditText titleEditText;
    private TextView selectedLocationTextView;
    private WebView reminderMapWebView;
    private LocationManager locationManager;
    private Double selectedLatitude;
    private Double selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_reminder);

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

        titleEditText = findViewById(R.id.reminderTitleEditText);
        selectedLocationTextView = findViewById(R.id.selectedLocationTextView);
        reminderMapWebView = findViewById(R.id.reminderMapWebView);
        Button useCurrentLocationButton = findViewById(R.id.useCurrentLocationButton);
        Button saveButton = findViewById(R.id.saveReminderButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        configureMap();

        if (getIntent().hasExtra(EXTRA_LATITUDE) && getIntent().hasExtra(EXTRA_LONGITUDE)) {
            selectLocation(
                    getIntent().getDoubleExtra(EXTRA_LATITUDE, DEFAULT_LATITUDE),
                    getIntent().getDoubleExtra(EXTRA_LONGITUDE, DEFAULT_LONGITUDE)
            );
        } else {
            loadMap(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }

        useCurrentLocationButton.setOnClickListener(v -> useCurrentGpsLocation());
        saveButton.setOnClickListener(v -> saveReminder());
    }

    private void configureMap() {
        reminderMapWebView.getSettings().setJavaScriptEnabled(true);
        reminderMapWebView.getSettings().setDomStorageEnabled(true);
        reminderMapWebView.addJavascriptInterface(new MapBridge(), "Android");
    }

    private void loadMap(double latitude, double longitude) {
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
                        "var map = L.map('map').setView([%.6f, %.6f], 15);" +
                        "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {" +
                        "maxZoom: 19, attribution: 'OpenStreetMap, CARTO'}).addTo(map);" +
                        "var marker = L.marker([%.6f, %.6f], { draggable: true }).addTo(map);" +
                        "function sendLocation(latlng) { Android.onLocationSelected(latlng.lat, latlng.lng); }" +
                        "marker.on('dragend', function(e) { sendLocation(e.target.getLatLng()); });" +
                        "map.on('click', function(e) { marker.setLatLng(e.latlng); sendLocation(e.latlng); });" +
                        "</script>" +
                        "</body>" +
                        "</html>",
                latitude, longitude, latitude, longitude);

        reminderMapWebView.loadDataWithBaseURL("https://carto.com/", html, "text/html", "UTF-8", null);
    }

    private void useCurrentGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        if (locationManager == null) {
            Toast.makeText(this, "Location service is unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Location lastKnownLocation = getBestLastKnownLocation();
            if (lastKnownLocation != null) {
                handleCurrentLocation(lastKnownLocation);
            }

            String provider = getBestEnabledProvider();
            if (provider == null) {
                Toast.makeText(this, "Please enable GPS or network location", Toast.LENGTH_LONG).show();
                return;
            }

            locationManager.getCurrentLocation(
                    provider,
                    null,
                    getMainExecutor(),
                    location -> {
                        if (location != null) {
                            handleCurrentLocation(location);
                        } else if (lastKnownLocation == null) {
                            Toast.makeText(this, "Current location is unavailable", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (SecurityException e) {
            Toast.makeText(this, "GPS permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private void handleCurrentLocation(Location location) {
        if (location == null) {
            Toast.makeText(this, "Current location is unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        selectLocation(location.getLatitude(), location.getLongitude());
    }

    private Location getBestLastKnownLocation() {
        Location gpsLocation = getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation == null) {
            return networkLocation;
        }

        if (networkLocation == null) {
            return gpsLocation;
        }

        return gpsLocation.getTime() >= networkLocation.getTime() ? gpsLocation : networkLocation;
    }

    private Location getLastKnownLocation(String provider) {
        if (locationManager == null || !locationManager.isProviderEnabled(provider)) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(provider);
        } catch (SecurityException e) {
            return null;
        }
    }

    private String getBestEnabledProvider() {
        if (locationManager == null) {
            return null;
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }

        return null;
    }

    private void selectLocation(double latitude, double longitude) {
        selectedLatitude = latitude;
        selectedLongitude = longitude;
        selectedLocationTextView.setText(String.format(
                Locale.US,
                "Selected location\nLatitude: %.6f\nLongitude: %.6f",
                latitude,
                longitude
        ));
        loadMap(latitude, longitude);
    }

    private void saveReminder() {
        String title = titleEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Enter reminder title");
            return;
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            Toast.makeText(this, "Choose a location on the map first", Toast.LENGTH_LONG).show();
            return;
        }

        ReminderStorage.saveReminder(this, title, selectedLatitude, selectedLongitude);
        Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                useCurrentGpsLocation();
            } else {
                Toast.makeText(this, "GPS permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class MapBridge {
        @JavascriptInterface
        public void onLocationSelected(double latitude, double longitude) {
            runOnUiThread(() -> {
                selectedLatitude = latitude;
                selectedLongitude = longitude;
                selectedLocationTextView.setText(String.format(
                        Locale.US,
                        "Selected location\nLatitude: %.6f\nLongitude: %.6f",
                        latitude,
                        longitude
                ));
            });
        }
    }
}
