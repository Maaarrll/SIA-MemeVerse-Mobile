package com.example.memeverseapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.memeverseapp.ui.fragments.HomeFragment;
import com.example.memeverseapp.ui.fragments.MessagesFragment;
import com.example.memeverseapp.ui.fragments.NotificationsFragment;
import com.example.memeverseapp.ui.fragments.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                openFragment(new HomeFragment(), "MemeVerse");
            } else if (id == R.id.nav_messages) {
                openFragment(new MessagesFragment(), "Messages");
            } else if (id == R.id.nav_notifications) {
                openFragment(new NotificationsFragment(), "Notifications");
            } else if (id == R.id.nav_settings) {
                openFragment(new SettingsFragment(), "Settings");
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
            openFragment(new HomeFragment(), "MemeVerse");
        }
    }

    private void openFragment(Fragment fragment, String title) {
        topAppBar.setTitle(title);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}