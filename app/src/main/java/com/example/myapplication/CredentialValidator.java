package com.example.myapplication;

import android.util.Patterns;

public class CredentialValidator {

    private CredentialValidator() {
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }
}
