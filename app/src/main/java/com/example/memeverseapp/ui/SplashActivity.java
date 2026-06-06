package com.example.memeverseapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memeverseapp.MainActivity;
import com.example.memeverseapp.R;
import com.example.memeverseapp.utils.PreferencesManager;

public class SplashActivity extends AppCompatActivity {

    private PreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefManager = new PreferencesManager(this);

        new Handler().postDelayed(() -> {
            Intent intent;

            if (prefManager.isLoggedIn() && prefManager.getUserId() > 0) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, 1500);
    }
}