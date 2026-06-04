package com.example.memeverseapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.LoginResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.network.RetrofitClient;
import com.example.memeverseapp.services.NotificationPollingService;
import com.example.memeverseapp.ui.MainActivity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

        apiService.login(login, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    prefManager.setUserId(response.body().getUser_id());
                    prefManager.setUsername(response.body().getUsername());
                    prefManager.setLoggedIn(true);

                    Intent serviceIntent = new Intent(getContext(), NotificationPollingService.class);
                    requireContext().startService(serviceIntent);

                    ToastUtils.showSuccess(requireActivity(), "Welcome back!");

                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();

                } else {
                    String error = response.body() != null ? response.body().getError() : "Login failed";
                    ToastUtils.showError(requireActivity(), error);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }
}