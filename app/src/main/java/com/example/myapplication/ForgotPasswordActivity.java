package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText forgotEmailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

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

        forgotEmailEditText = findViewById(R.id.forgotEmailEditText);
        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);
        Button backToLoginButton = findViewById(R.id.forgotBackToLoginButton);

        resetPasswordButton.setOnClickListener(v -> showResetPopup());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void showResetPopup() {
        String email = forgotEmailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            forgotEmailEditText.setError("Enter the Email");
            return;
        }

        if (!CredentialValidator.isValidEmail(email)) {
            forgotEmailEditText.setError("Enter a valid Email");
            return;
        }

        if (!LocalAccountStorage.isSavedEmail(this, email)) {
            showPopup(
                    "Account not found",
                    "No local account is saved with this email. Create an account first."
            );
            return;
        }

        showPopup(
                "Reset request received",
                "This project uses local SharedPreferences only, so no real email is sent. Use the password you created for this local account."
        );
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
