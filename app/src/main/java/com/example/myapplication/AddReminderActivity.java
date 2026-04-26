package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddReminderActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_reminder);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleEditText = findViewById(R.id.reminderTitleEditText);
        latitudeEditText = findViewById(R.id.reminderLatitudeEditText);
        longitudeEditText = findViewById(R.id.reminderLongitudeEditText);
        Button saveButton = findViewById(R.id.saveReminderButton);

        saveButton.setOnClickListener(v -> saveReminder());
    }

    private void saveReminder() {
        String title = titleEditText.getText().toString().trim();
        String latitudeText = latitudeEditText.getText().toString().trim();
        String longitudeText = longitudeEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Enter reminder title");
            return;
        }

        if (latitudeText.isEmpty()) {
            latitudeEditText.setError("Enter latitude");
            return;
        }

        if (longitudeText.isEmpty()) {
            longitudeEditText.setError("Enter longitude");
            return;
        }

        try {
            double latitude = Double.parseDouble(latitudeText);
            double longitude = Double.parseDouble(longitudeText);

            if (latitude < -90 || latitude > 90) {
                latitudeEditText.setError("Latitude must be between -90 and 90");
                return;
            }

            if (longitude < -180 || longitude > 180) {
                longitudeEditText.setError("Longitude must be between -180 and 180");
                return;
            }

            ReminderStorage.saveReminder(this, title, latitude, longitude);
            Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Latitude and longitude must be numbers", Toast.LENGTH_LONG).show();
        }
    }
}
