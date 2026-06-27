package com.andika.temucashflow.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "TemuCashflowPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_THEME_MODE = "theme_mode"; // 0: System, 1: Light, 2: Dark
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget";
    private static final String KEY_SHAKE_ENABLED = "shake_enabled";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_TTS = "tts_enabled";
    private static final String KEY_SPEECH_RECOG = "speech_recog_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";

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

    public void setThemeMode(int mode) {
        editor.putInt(KEY_THEME_MODE, mode);
        editor.apply();
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME_MODE, 0); // Default System
    }

    public boolean isDarkMode() {
        // Keeping for backward compatibility or simple checks if needed, 
        // but setThemeMode is preferred now.
        return getThemeMode() == 2;
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

    public void setShakeEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_SHAKE_ENABLED, isEnabled);
        editor.apply();
    }

    public boolean isShakeEnabled() {
        return sharedPreferences.getBoolean(KEY_SHAKE_ENABLED, true); // Default true
    }

    public void setFontSize(int size) {
        editor.putInt(KEY_FONT_SIZE, size).apply();
    }

    public int getFontSize() {
        return sharedPreferences.getInt(KEY_FONT_SIZE, 1);
    }

    public void setHighContrastEnabled(boolean enabled) {
        editor.putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }

    public boolean isHighContrastEnabled() {
        return sharedPreferences.getBoolean(KEY_HIGH_CONTRAST, false);
    }

    public void setTtsEnabled(boolean enabled) {
        editor.putBoolean(KEY_TTS, enabled).apply();
    }

    public boolean isTtsEnabled() {
        return sharedPreferences.getBoolean(KEY_TTS, false);
    }

    public void setSpeechRecognitionEnabled(boolean enabled) {
        editor.putBoolean(KEY_SPEECH_RECOG, enabled).apply();
    }

    public boolean isSpeechRecognitionEnabled() {
        return sharedPreferences.getBoolean(KEY_SPEECH_RECOG, false);
    }

    public void setVibrationEnabled(boolean enabled) {
        editor.putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION, true);
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
