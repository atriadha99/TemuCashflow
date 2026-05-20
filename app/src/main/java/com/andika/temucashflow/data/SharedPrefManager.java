package com.andika.temucashflow.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "TemuCashflowPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget";

    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(long userId, String name, String email) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void setDarkMode(boolean isEnabled) {
        editor.putBoolean(KEY_DARK_MODE, isEnabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setBiometricEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_BIOMETRIC_ENABLED, isEnabled);
        editor.apply();
    }

    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setReminderEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_REMINDER_ENABLED, isEnabled);
        editor.apply();
    }

    public boolean isReminderEnabled() {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, true); // Default true
    }

    public void setMonthlyBudget(float budget) {
        editor.putFloat(KEY_MONTHLY_BUDGET, budget);
        editor.apply();
    }

    public float getMonthlyBudget() {
        return sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0); // Default 0 (no budget set)
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
