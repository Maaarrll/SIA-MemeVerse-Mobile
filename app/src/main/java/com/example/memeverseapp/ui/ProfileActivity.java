package com.example.memeverseapp.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memeverseapp.R;
import com.example.memeverseapp.utils.PreferencesManager;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        PreferencesManager prefManager = new PreferencesManager(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_container, ProfileFragment.newInstance(prefManager.getUserId()))
                    .commit();
        }
    }
}