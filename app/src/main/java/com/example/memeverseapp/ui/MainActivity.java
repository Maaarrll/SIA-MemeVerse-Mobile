package com.example.memeverseapp.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.UserResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.network.RetrofitClient;
import com.example.memeverseapp.services.NotificationPollingService;
import com.example.memeverseapp.ui.fragments.HomeFragment;
import com.example.memeverseapp.ui.fragments.NotificationsFragment;
import com.example.memeverseapp.ui.fragments.SearchFragment;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnHome, btnSearch, btnUpload, btnSettings, btnMessages, btnNotifications;
    private TextView badgeMessages, badgeNotifications;
    private EditText searchEditText;

    private PreferencesManager prefManager;
    private ApiService apiService;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefManager = new PreferencesManager(this);

        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnUpload = findViewById(R.id.btnUpload);
        btnSettings = findViewById(R.id.btnSettings);
        btnMessages = findViewById(R.id.btnMessages);
        btnNotifications = findViewById(R.id.btnNotifications);

        badgeMessages = findViewById(R.id.badgeMessages);
        badgeNotifications = findViewById(R.id.badgeNotifications);
        searchEditText = findViewById(R.id.searchEditText);

        btnHome.setOnClickListener(v -> loadFragment(new HomeFragment()));

        btnSearch.setOnClickListener(v -> loadFragment(new SearchFragment()));

        btnUpload.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UploadActivity.class))
        );

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnMessages.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MessagesActivity.class))
        );

        btnNotifications.setOnClickListener(v ->
                loadFragment(new NotificationsFragment())
        );

        setupSearchBar();
        observeBadges();
        startPollingService();
        validateSession();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void validateSession() {
        int userId = prefManager.getUserId();

        if (userId == 0) {
            sessionExpired();
            return;
        }

        apiService.getUser(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.code() == 401) {
                    sessionExpired();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Do not logout on network error.
                // Only logout when server clearly returns 401.
            }
        });
    }

    private void sessionExpired() {
        prefManager.clear();

        Intent serviceIntent = new Intent(this, NotificationPollingService.class);
        stopService(serviceIntent);

        ToastUtils.showError(this, "Session expired. Please login again.");

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (query.length() >= 2) {
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 300);
                } else {
                    Fragment current = getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);

                    if (current instanceof SearchFragment) {
                        ((SearchFragment) current).performSearch("");
                    }
                }
            }
        });
    }

    private void performSearch(String query) {
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (current instanceof SearchFragment) {
            ((SearchFragment) current).performSearch(query);
        } else {
            SearchFragment fragment = new SearchFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            new Handler().postDelayed(() -> {
                Fragment now = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                if (now instanceof SearchFragment) {
                    ((SearchFragment) now).performSearch(query);
                }
            }, 100);
        }
    }

    private void showSettingsDialog() {
        String[] options = {
                "Toggle Dark Mode",
                "Change Server URL",
                "Clear Image Cache",
                "Logout"
        };

        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        toggleDarkMode();
                    } else if (which == 1) {
                        showServerUrlDialog();
                    } else if (which == 2) {
                        clearImageCache();
                    } else if (which == 3) {
                        logout();
                    }
                })
                .show();
    }

    private void toggleDarkMode() {
        boolean newDarkMode = !prefManager.isDarkMode();
        prefManager.setDarkMode(newDarkMode);

        if (newDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        recreate();
    }

    private void showServerUrlDialog() {
        EditText input = new EditText(this);
        input.setHint("http://192.168.1.2/memeverse/");
        input.setText(prefManager.getBaseUrl());
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Server URL")
                .setMessage("Enter your backend base URL.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUrl = input.getText().toString().trim();

                    if (newUrl.isEmpty()) {
                        ToastUtils.showError(this, "URL cannot be empty");
                        return;
                    }

                    if (!newUrl.endsWith("/")) {
                        newUrl = newUrl + "/";
                    }

                    RetrofitClient.updateBaseUrl(this, newUrl);
                    ToastUtils.showSuccess(this, "Server URL updated");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearImageCache() {
        new Thread(() -> {
            try {
                com.bumptech.glide.Glide.get(this).clearDiskCache();

                runOnUiThread(() ->
                        ToastUtils.showSuccess(this, "Image cache cleared")
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        ToastUtils.showError(this, "Failed to clear cache")
                );
            }
        }).start();

        try {
            com.bumptech.glide.Glide.get(this).clearMemory();
        } catch (Exception ignored) {
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    prefManager.clear();

                    Intent serviceIntent = new Intent(this, NotificationPollingService.class);
                    stopService(serviceIntent);

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeBadges() {
        NotificationPollingService.getUnreadNotificationsCount().observe(this, count ->
                updateBadge(badgeNotifications, count)
        );

        NotificationPollingService.getUnreadMessagesCount().observe(this, count ->
                updateBadge(badgeMessages, count)
        );
    }

    private void updateBadge(TextView badge, int count) {
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void startPollingService() {
        Intent intent = new Intent(this, NotificationPollingService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}