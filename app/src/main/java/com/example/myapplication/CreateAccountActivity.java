package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

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

        emailEditText = findViewById(R.id.createEmailEditText);
        passwordEditText = findViewById(R.id.createPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        Button saveAccountButton = findViewById(R.id.saveAccountButton);
        Button backToLoginButton = findViewById(R.id.backToLoginButton);

        saveAccountButton.setOnClickListener(v -> createAccount());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (email.isEmpty()) {
            emailEditText.setError("Enter the Email");
            return;
        }

        if (!CredentialValidator.isValidEmail(email)) {
            emailEditText.setError("Enter a valid Email");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter the Password");
            return;
        }

        if (!CredentialValidator.isStrongPassword(password)) {
            passwordEditText.setError("Password must be 8+ chars with letters, numbers, and symbols");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        LocalAccountStorage.saveAccount(this, email, password);
        Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
        finish();
    }
}
