package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "account_prefs";
    private static final String KEY_ACCOUNT_EMAIL = "account_email";
    private static final String KEY_ACCOUNT_PASSWORD = "account_password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    private static final String KEY_REMEMBERED_PASSWORD = "remembered_password";

    private ActivityMainBinding binding;
    private SharedPreferences accountPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText userEmail = findViewById(R.id.editTextUserId);
        EditText userPassword = findViewById(R.id.editTextPassword);
        CheckBox rememberMeCheckBox = findViewById(R.id.checkBox);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        accountPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (accountPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            rememberMeCheckBox.setChecked(true);
            userEmail.setText(accountPreferences.getString(KEY_REMEMBERED_EMAIL, ""));
            userPassword.setText(accountPreferences.getString(KEY_REMEMBERED_PASSWORD, ""));
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString();

                if (!validateCredentials(userEmail, userPassword, email, password)) {
                    return;
                }

                String savedEmail = accountPreferences.getString(KEY_ACCOUNT_EMAIL, "");
                String savedPassword = accountPreferences.getString(KEY_ACCOUNT_PASSWORD, "");
                if (!email.equals(savedEmail) || !password.equals(savedPassword)) {
                    Toast.makeText(MainActivity.this, "Create an account first or check your login details", Toast.LENGTH_LONG).show();
                    return;
                }

                saveRememberMeChoice(rememberMeCheckBox.isChecked(), email, password);
                openHome(email);
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString();

            if (!validateCredentials(userEmail, userPassword, email, password)) {
                return;
            }

            accountPreferences.edit()
                    .putString(KEY_ACCOUNT_EMAIL, email)
                    .putString(KEY_ACCOUNT_PASSWORD, password)
                    .apply();
            saveRememberMeChoice(rememberMeCheckBox.isChecked(), email, password);
            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
            openHome(email);
        });
    }

    private boolean validateCredentials(EditText emailEditText, EditText passwordEditText,
                                        String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Enter the Email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid Email");
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter the Password");
            return false;
        }

        if (!isStrongPassword(password)) {
            passwordEditText.setError("Password must be 8+ chars with letters, numbers, and symbols");
            return false;
        }

        return true;
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    private void saveRememberMeChoice(boolean rememberMe, String email, String password) {
        SharedPreferences.Editor editor = accountPreferences.edit()
                .putBoolean(KEY_REMEMBER_ME, rememberMe);

        if (rememberMe) {
            editor.putString(KEY_REMEMBERED_EMAIL, email)
                    .putString(KEY_REMEMBERED_PASSWORD, password);
        } else {
            editor.remove(KEY_REMEMBERED_EMAIL)
                    .remove(KEY_REMEMBERED_PASSWORD);
        }

        editor.apply();
    }

    private void openHome(String email) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("UserId", email);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;    
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }
}
