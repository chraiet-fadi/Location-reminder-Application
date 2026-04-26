package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private WebView mapWebView;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_current_location);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        latitudeTextView = findViewById(R.id.currentLatitudeTextView);
        longitudeTextView = findViewById(R.id.currentLongitudeTextView);
        mapWebView = findViewById(R.id.currentLocationMapWebView);
        Button refreshButton = findViewById(R.id.refreshLocationButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mapWebView.getSettings().setJavaScriptEnabled(true);
        mapWebView.loadData(
                "<html><body style='font-family:sans-serif;text-align:center;padding-top:40px;'>Waiting for GPS location...</body></html>",
                "text/html",
                "UTF-8"
        );

        refreshButton.setOnClickListener(v -> loadCurrentGpsLocation());
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

        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            latitudeTextView.setText(R.string.latitude_unavailable);
            longitudeTextView.setText(R.string.longitude_unavailable);
            Toast.makeText(this, "Please enable GPS to get your location", Toast.LENGTH_LONG).show();
            return;
        }

        latitudeTextView.setText(R.string.latitude_loading);
        longitudeTextView.setText(R.string.longitude_loading);

        try {
            locationManager.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    null,
                    getMainExecutor(),
                    this::displayLocation
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
        showLocationOnMap(location);
    }

    private void showLocationOnMap(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String mapUrl = String.format(
                Locale.US,
                "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=17/%.6f/%.6f",
                latitude,
                longitude,
                latitude,
                longitude
        );

        mapWebView.loadUrl(mapUrl);
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
