package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAccountStorage {

    private static final String PREFS_NAME = "account_prefs";
    private static final String KEY_ACCOUNT_EMAIL = "account_email";
    private static final String KEY_ACCOUNT_PASSWORD = "account_password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    private static final String KEY_REMEMBERED_PASSWORD = "remembered_password";

    private LocalAccountStorage() {
    }

    public static void saveAccount(Context context, String email, String password) {
        getPreferences(context).edit()
                .putString(KEY_ACCOUNT_EMAIL, email)
                .putString(KEY_ACCOUNT_PASSWORD, password)
                .apply();
    }

    public static boolean isValidAccount(Context context, String email, String password) {
        SharedPreferences preferences = getPreferences(context);
        String savedEmail = preferences.getString(KEY_ACCOUNT_EMAIL, "");
        String savedPassword = preferences.getString(KEY_ACCOUNT_PASSWORD, "");
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    public static boolean isRememberMeEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_REMEMBER_ME, false);
    }

    public static String getRememberedEmail(Context context) {
        return getPreferences(context).getString(KEY_REMEMBERED_EMAIL, "");
    }

    public static String getRememberedPassword(Context context) {
        return getPreferences(context).getString(KEY_REMEMBERED_PASSWORD, "");
    }

    public static void saveRememberMeChoice(Context context, boolean rememberMe,
                                            String email, String password) {
        SharedPreferences.Editor editor = getPreferences(context).edit()
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

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
