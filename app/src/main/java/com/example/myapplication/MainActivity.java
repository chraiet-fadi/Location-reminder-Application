package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
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

    private ActivityMainBinding binding;

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

        if (LocalAccountStorage.isRememberMeEnabled(this)) {
            rememberMeCheckBox.setChecked(true);
            userEmail.setText(LocalAccountStorage.getRememberedEmail(this));
            userPassword.setText(LocalAccountStorage.getRememberedPassword(this));
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString();

                if (!validateCredentials(userEmail, userPassword, email, password)) {
                    return;
                }

                if (!LocalAccountStorage.isValidAccount(MainActivity.this, email, password)) {
                    Toast.makeText(MainActivity.this, "Create an account first or check your login details", Toast.LENGTH_LONG).show();
                    return;
                }

                LocalAccountStorage.saveRememberMeChoice(
                        MainActivity.this,
                        rememberMeCheckBox.isChecked(),
                        email,
                        password
                );
                openHome(email);
            }
        });

        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateAccountActivity.class)));
    }

    private boolean validateCredentials(EditText emailEditText, EditText passwordEditText,
                                        String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Enter the Email");
            return false;
        }

        if (!CredentialValidator.isValidEmail(email)) {
            emailEditText.setError("Enter a valid Email");
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter the Password");
            return false;
        }

        if (!CredentialValidator.isStrongPassword(password)) {
            passwordEditText.setError("Password must be 8+ chars with letters, numbers, and symbols");
            return false;
        }

        return true;
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
