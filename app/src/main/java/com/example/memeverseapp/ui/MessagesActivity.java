package com.example.memeverseapp.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memeverseapp.R;
import com.example.memeverseapp.ui.fragments.MessagesFragment;

public class MessagesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.messages_container, new MessagesFragment())
                    .commit();
        }
    }
}