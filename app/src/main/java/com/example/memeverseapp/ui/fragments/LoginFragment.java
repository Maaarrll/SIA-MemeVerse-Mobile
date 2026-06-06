package com.example.memeverseapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.memeverseapp.MainActivity;
import com.example.memeverseapp.R;
import com.example.memeverseapp.models.LoginResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.NotificationPollingService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etLogin, etPassword;
    private Button btnLogin;

    private ApiService apiService;
    private PreferencesManager prefManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etLogin = view.findViewById(R.id.etLogin);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        prefManager = new PreferencesManager(requireContext());

        btnLogin.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                ToastUtils.showError(requireActivity(), "Please fill all fields");
                return;
            }

            performLogin(login, password);
        });

        return view;
    }

    private void performLogin(String login, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        apiService.login(login, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<LoginResponse> call,
                    @NonNull Response<LoginResponse> response
            ) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse loginResponse = response.body();

                    prefManager.saveLogin(
                            loginResponse.getUser_id(),
                            loginResponse.getUsername()
                    );

                    try {
                        Intent serviceIntent = new Intent(requireContext(), NotificationPollingService.class);
                        requireContext().startService(serviceIntent);
                    } catch (Exception ignored) {
                    }

                    ToastUtils.showSuccess(requireActivity(), "Welcome back!");

                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();

                } else {
                    String error = "Login failed";

                    if (response.body() != null && response.body().getError() != null) {
                        error = response.body().getError();
                    }

                    ToastUtils.showError(requireActivity(), error);
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<LoginResponse> call,
                    @NonNull Throwable t
            ) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }
}