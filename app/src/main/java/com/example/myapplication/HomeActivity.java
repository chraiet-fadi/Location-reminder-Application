package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    private static final float REMINDER_RADIUS_METERS = 50f;

    private TextView reminderStatusTextView;
    private TextView reminderDistanceTextView;
    private LocationManager locationManager;
    private boolean reminderDialogShown;

    private final LocationListener reminderLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            checkReminderDistance(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

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

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        reminderStatusTextView = findViewById(R.id.reminderStatusTextView);
        reminderDistanceTextView = findViewById(R.id.reminderDistanceTextView);
        Button addReminderButton = findViewById(R.id.addReminderButton);
        Button currentLocationButton = findViewById(R.id.currentLocationButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String userId = getIntent().getStringExtra("UserId");
        if (userId != null) {
            welcomeTextView.setText(String.format(Locale.US, "Welcome %s", userId));
        }

        addReminderButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AddReminderActivity.class)));
        currentLocationButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, CurrentLocationActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        reminderDialogShown = false;
        updateReminderStatus();
        startReminderMonitoring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReminderMonitoring();
    }

    private void updateReminderStatus() {
        if (!ReminderStorage.hasReminder(this)) {
            reminderStatusTextView.setText(R.string.no_reminder_saved);
            reminderDistanceTextView.setText(R.string.distance_waiting);
            return;
        }

        reminderStatusTextView.setText(String.format(
                Locale.US,
                "Saved reminder: %s\nLatitude: %.6f\nLongitude: %.6f",
                ReminderStorage.getTitle(this),
                ReminderStorage.getLatitude(this),
                ReminderStorage.getLongitude(this)
        ));
        reminderDistanceTextView.setText(R.string.distance_waiting);
    }

    private void startReminderMonitoring() {
        if (!ReminderStorage.hasReminder(this)) {
            return;
        }

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
            reminderDistanceTextView.setText(R.string.gps_disabled);
            Toast.makeText(this, "Location service is unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Location lastKnownLocation = getBestLastKnownLocation();
            if (lastKnownLocation != null) {
                checkReminderDistance(lastKnownLocation);
            }

            String provider = getBestEnabledProvider();
            if (provider == null) {
                reminderDistanceTextView.setText(R.string.gps_disabled);
                Toast.makeText(this, "Please enable GPS or network location", Toast.LENGTH_LONG).show();
                return;
            }

            locationManager.requestLocationUpdates(
                    provider,
                    5000,
                    5,
                    reminderLocationListener
            );
        } catch (SecurityException e) {
            reminderDistanceTextView.setText(R.string.latitude_permission_denied);
        }
    }

    private void stopReminderMonitoring() {
        if (locationManager == null) {
            return;
        }

        locationManager.removeUpdates(reminderLocationListener);
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

    private void checkReminderDistance(Location currentLocation) {
        double reminderLatitude = ReminderStorage.getLatitude(this);
        double reminderLongitude = ReminderStorage.getLongitude(this);
        float[] results = new float[1];

        Location.distanceBetween(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                reminderLatitude,
                reminderLongitude,
                results
        );

        float distance = results[0];
        reminderDistanceTextView.setText(String.format(
                Locale.US,
                "Distance to reminder: %.1f meters",
                distance
        ));

        if (distance <= REMINDER_RADIUS_METERS && !reminderDialogShown) {
            reminderDialogShown = true;
            showReminderReachedDialog();
        }
    }

    private void showReminderReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reminder reached")
                .setMessage(ReminderStorage.getTitle(this))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startReminderMonitoring();
            } else {
                reminderDistanceTextView.setText(R.string.latitude_permission_denied);
                Toast.makeText(this, "GPS permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
