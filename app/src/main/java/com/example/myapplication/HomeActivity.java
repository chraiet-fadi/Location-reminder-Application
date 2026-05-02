package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2002;
    private static final int REMINDER_NEAR_NOTIFICATION_ID = 100;
    private static final String REMINDER_CHANNEL_ID = "reminder_location_alerts";
    private static final float REMINDER_RADIUS_METERS = 50f;
    private static final float REMINDER_NOTIFICATION_RADIUS_METERS = 200f;

    private TextView reminderStatusTextView;
    private TextView reminderDistanceTextView;
    private LinearLayout remindersListLayout;
    private LocationManager locationManager;
    private final Set<String> dialogReminderIds = new HashSet<>();
    private final Set<String> notificationReminderIds = new HashSet<>();

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
        remindersListLayout = findViewById(R.id.remindersListLayout);
        ScrollView homeScrollView = findViewById(R.id.homeScrollView);
        BottomNavigationView homeBottomNavigation = findViewById(R.id.homeBottomNavigation);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createReminderNotificationChannel();
        requestNotificationPermissionIfNeeded();

        String userId = getIntent().getStringExtra("UserId");
        if (userId != null) {
            welcomeTextView.setText(String.format(Locale.US, "Welcome %s", userId));
        }

        homeBottomNavigation.setSelectedItemId(R.id.nav_saved_reminders);
        homeBottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_saved_reminders) {
                homeScrollView.smoothScrollTo(0, reminderStatusTextView.getTop());
                return true;
            }

            if (itemId == R.id.nav_add_reminder) {
                startActivity(new Intent(HomeActivity.this, AddReminderActivity.class));
                return false;
            }

            if (itemId == R.id.nav_current_location) {
                startActivity(new Intent(HomeActivity.this, CurrentLocationActivity.class));
                return false;
            }

            if (itemId == R.id.nav_logout) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            remindersListLayout.removeAllViews();
            return;
        }

        List<ReminderStorage.Reminder> reminders = ReminderStorage.getReminders(this);
        reminderStatusTextView.setText(String.format(Locale.US, "Saved reminders: %d", reminders.size()));
        renderSavedReminders(reminders);
        reminderDistanceTextView.setText(R.string.distance_waiting);
    }

    private void renderSavedReminders(List<ReminderStorage.Reminder> reminders) {
        remindersListLayout.removeAllViews();

        for (ReminderStorage.Reminder reminder : reminders) {
            TextView reminderView = new TextView(this);
            reminderView.setText(String.format(
                    Locale.US,
                    "%s\nLatitude: %.6f\nLongitude: %.6f",
                    reminder.getTitle(),
                    reminder.getLatitude(),
                    reminder.getLongitude()
            ));
            reminderView.setTextColor(ContextCompat.getColor(this, R.color.app_primary_dark));
            reminderView.setTextSize(16);
            reminderView.setGravity(Gravity.CENTER);
            reminderView.setLineSpacing(4, 1);
            reminderView.setPadding(18, 18, 18, 18);
            reminderView.setBackgroundResource(R.drawable.panel_background);
            reminderView.setClickable(true);
            reminderView.setOnClickListener(v -> openReminderMap(reminder.getId()));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 14);
            remindersListLayout.addView(reminderView, layoutParams);
        }
    }

    private void openReminderMap(String reminderId) {
        Intent intent = new Intent(this, ReminderMapActivity.class);
        intent.putExtra(ReminderMapActivity.EXTRA_REMINDER_ID, reminderId);
        startActivity(intent);
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
        List<ReminderStorage.Reminder> reminders = ReminderStorage.getReminders(this);
        ReminderStorage.Reminder closestReminder = null;
        float closestDistance = Float.MAX_VALUE;
        float[] results = new float[1];

        for (ReminderStorage.Reminder reminder : reminders) {
            Location.distanceBetween(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    reminder.getLatitude(),
                    reminder.getLongitude(),
                    results
            );

            float distance = results[0];
            if (distance < closestDistance) {
                closestDistance = distance;
                closestReminder = reminder;
            }

            if (distance <= REMINDER_NOTIFICATION_RADIUS_METERS
                    && notificationReminderIds.add(reminder.getId())) {
                showNearReminderNotification(reminder, distance);
            }

            if (distance <= REMINDER_RADIUS_METERS && dialogReminderIds.add(reminder.getId())) {
                showReminderReachedDialog(reminder);
            }
        }

        if (closestReminder == null) {
            reminderDistanceTextView.setText(R.string.distance_waiting);
            return;
        }

        reminderDistanceTextView.setText(String.format(
                Locale.US,
                "Closest reminder: %s\nDistance: %.1f meters",
                closestReminder.getTitle(),
                closestDistance
        ));
    }

    private void createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Location reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alerts when you are close to a saved reminder location");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void showNearReminderNotification(ReminderStorage.Reminder reminder, float distance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, ReminderMapActivity.class);
        intent.putExtra(ReminderMapActivity.EXTRA_REMINDER_ID, reminder.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String content = String.format(
                Locale.US,
                "You are %.0f meters from: %s",
                distance,
                reminder.getTitle()
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reminder nearby")
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this)
                .notify(REMINDER_NEAR_NOTIFICATION_ID + Math.abs(reminder.getId().hashCode()), builder.build());
    }

    private void showReminderReachedDialog(ReminderStorage.Reminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Reminder reached")
                .setMessage(reminder.getTitle())
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
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
