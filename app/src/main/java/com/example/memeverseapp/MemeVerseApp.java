package com.example.memeverseapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.memeverseapp.network.RetrofitClient;
import com.example.memeverseapp.utils.PreferencesManager;

public class MemeVerseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferencesManager prefManager = new PreferencesManager(this);

        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        RetrofitClient.init(this);
    }
}