package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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

public class CurrentLocationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3001;
    private static final double DEFAULT_LATITUDE = 34.8504;
    private static final double DEFAULT_LONGITUDE = 5.7281;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private WebView mapWebView;
    private LocationManager locationManager;
    private Double currentLatitude;
    private Double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_current_location);

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

        latitudeTextView = findViewById(R.id.currentLatitudeTextView);
        longitudeTextView = findViewById(R.id.currentLongitudeTextView);
        mapWebView = findViewById(R.id.currentLocationMapWebView);
        Button refreshButton = findViewById(R.id.refreshLocationButton);
        Button addReminderHereButton = findViewById(R.id.addReminderHereButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mapWebView.getSettings().setJavaScriptEnabled(true);
        mapWebView.getSettings().setDomStorageEnabled(true);
        showMap(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);

        refreshButton.setOnClickListener(v -> loadCurrentGpsLocation());
        addReminderHereButton.setOnClickListener(v -> openReminderForCurrentLocation());
        loadCurrentGpsLocation();
    }

    private void loadCurrentGpsLocation() {
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
            latitudeTextView.setText(R.string.latitude_unavailable);
            longitudeTextView.setText(R.string.longitude_unavailable);
            Toast.makeText(this, "Location service is unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        latitudeTextView.setText(R.string.latitude_loading);
        longitudeTextView.setText(R.string.longitude_loading);

        try {
            Location lastKnownLocation = getBestLastKnownLocation();
            if (lastKnownLocation != null) {
                displayLocation(lastKnownLocation);
            }

            String provider = getBestEnabledProvider();
            if (provider == null) {
                latitudeTextView.setText(R.string.latitude_unavailable);
                longitudeTextView.setText(R.string.longitude_unavailable);
                Toast.makeText(this, "Please enable GPS or network location", Toast.LENGTH_LONG).show();
                return;
            }

            locationManager.getCurrentLocation(
                    provider,
                    null,
                    getMainExecutor(),
                    location -> {
                        if (location != null) {
                            displayLocation(location);
                        } else if (lastKnownLocation == null) {
                            latitudeTextView.setText(R.string.latitude_unavailable);
                            longitudeTextView.setText(R.string.longitude_unavailable);
                            Toast.makeText(this, "Current location is unavailable", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (SecurityException e) {
            latitudeTextView.setText(R.string.latitude_permission_denied);
            longitudeTextView.setText(R.string.longitude_permission_denied);
        }
    }

    private void displayLocation(Location location) {
        if (location == null) {
            latitudeTextView.setText(R.string.latitude_unavailable);
            longitudeTextView.setText(R.string.longitude_unavailable);
            return;
        }

        latitudeTextView.setText(String.format(Locale.US, "Latitude: %.6f", location.getLatitude()));
        longitudeTextView.setText(String.format(Locale.US, "Longitude: %.6f", location.getLongitude()));
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        showMap(currentLatitude, currentLongitude);
    }

    private void openReminderForCurrentLocation() {
        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(this, "Wait for GPS location first", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra(AddReminderActivity.EXTRA_LATITUDE, currentLatitude);
        intent.putExtra(AddReminderActivity.EXTRA_LONGITUDE, currentLongitude);
        startActivity(intent);
    }

    private void showMap(double latitude, double longitude) {
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
                        "L.marker([%.6f, %.6f]).addTo(map);" +
                        "</script>" +
                        "</body>" +
                        "</html>",
                latitude, longitude, latitude, longitude);

        mapWebView.loadDataWithBaseURL("https://carto.com/", html, "text/html", "UTF-8", null);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCurrentGpsLocation();
            } else {
                latitudeTextView.setText(R.string.latitude_permission_denied);
                longitudeTextView.setText(R.string.longitude_permission_denied);
                Toast.makeText(this, "GPS permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
