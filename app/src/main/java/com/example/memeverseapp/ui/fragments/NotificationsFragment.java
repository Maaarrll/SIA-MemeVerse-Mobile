package com.example.memeverseapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.NotificationAdapter;
import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.AppNotification;
import com.example.memeverseapp.models.NotificationsResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, btnMarkAllRead;

    private ApiService apiService;
    private NotificationAdapter adapter;

    private final List<AppNotification> notifications = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        adapter = new NotificationAdapter(notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getLatestNotifications().enqueue(new Callback<NotificationsResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<NotificationsResponse> call,
                    @NonNull Response<NotificationsResponse> response
            ) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    notifications.clear();

                    if (response.body().getNotifications() != null) {
                        notifications.addAll(response.body().getNotifications());
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load notifications");
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<NotificationsResponse> call,
                    @NonNull Throwable t
            ) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
                updateEmptyState();
            }
        });
    }

    private void markAllAsRead() {
        apiService.markAllNotificationsRead().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse> call,
                    @NonNull Response<ApiResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "All notifications marked as read");
                    loadNotifications();
                } else {
                    ToastUtils.showError(requireActivity(), "Failed to update notifications");
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse> call,
                    @NonNull Throwable t
            ) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}