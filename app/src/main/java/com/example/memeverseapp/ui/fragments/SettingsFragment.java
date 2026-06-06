package com.example.memeverseapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.memeverseapp.R;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.ui.LoginActivity;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private LinearLayout btnProfile, btnBaseUrl, btnAbout;
    private TextView tvBaseUrl;
    private android.widget.Button btnLogout;

    private PreferencesManager prefManager;
    private ApiService apiService;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefManager = new PreferencesManager(requireContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnProfile = view.findViewById(R.id.btnProfile);
        btnBaseUrl = view.findViewById(R.id.btnBaseUrl);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvBaseUrl = view.findViewById(R.id.tvBaseUrl);

        tvBaseUrl.setText(prefManager.getBaseUrl());

        setupClicks();

        return view;
    }

    private void setupClicks() {
        btnProfile.setOnClickListener(v -> openProfile());

        btnBaseUrl.setOnClickListener(v -> showBaseUrlDialog());

        btnAbout.setOnClickListener(v -> showAboutDialog());

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void openProfile() {
        int userId = prefManager.getUserId();

        if (userId <= 0) {
            ToastUtils.showError(requireActivity(), "User not found");
            return;
        }

        ProfileFragment fragment = ProfileFragment.newInstance(userId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showBaseUrlDialog() {
        EditText input = new EditText(requireContext());
        input.setText(prefManager.getBaseUrl());
        input.setSingleLine(true);
        input.setHint("http://10.0.2.2/memeverse/");

        new AlertDialog.Builder(requireContext())
                .setTitle("Change API Base URL")
                .setMessage("Use 10.0.2.2 for Android Emulator. Use your computer IPv4 for real phone testing.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUrl = input.getText().toString().trim();

                    if (newUrl.isEmpty()) {
                        ToastUtils.showError(requireActivity(), "Base URL cannot be empty");
                        return;
                    }

                    if (!newUrl.endsWith("/")) {
                        newUrl = newUrl + "/";
                    }

                    RetrofitClient.updateBaseUrl(requireContext(), newUrl);
                    tvBaseUrl.setText(newUrl);

                    ToastUtils.showSuccess(requireActivity(), "Base URL updated");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About MemeVerse")
                .setMessage(
                        "MemeVerse is a meme sharing mobile app connected to a PHP/MySQL web backend.\n\n" +
                                "Features:\n" +
                                "• Browse memes\n" +
                                "• Upload posts\n" +
                                "• Vote and comment\n" +
                                "• Chat with users\n" +
                                "• Profile management\n\n" +
                                "Developed by Marl June S. Ordonia."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        apiService.logout().enqueue(new Callback<com.example.memeverseapp.models.ApiResponse>() {
            @Override
            public void onResponse(
                    Call<com.example.memeverseapp.models.ApiResponse> call,
                    Response<com.example.memeverseapp.models.ApiResponse> response
            ) {
                finishLogout();
            }

            @Override
            public void onFailure(
                    Call<com.example.memeverseapp.models.ApiResponse> call,
                    Throwable t
            ) {
                finishLogout();
            }
        });
    }

    private void finishLogout() {
        RetrofitClient.clearCookies();
        prefManager.logout();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}