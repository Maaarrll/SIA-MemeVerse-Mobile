package com.example.memeverseapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    private static final String PREF_NAME = "MemeVersePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setUserId(int id) {
        prefs.edit().putInt(KEY_USER_ID, id).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public void setBaseUrl(String url) {
        prefs.edit().putString(KEY_BASE_URL, url).apply();
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, "http://10.0.2.2/memeverse/");
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void clear() {
        boolean darkMode = isDarkMode();
        String baseUrl = getBaseUrl();

        prefs.edit().clear().apply();

        setDarkMode(darkMode);
        setBaseUrl(baseUrl);
    }
}