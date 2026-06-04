package com.example.memeverseapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.NotificationAdapter;
import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.Notification;
import com.example.memeverseapp.models.NotificationsResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.network.RetrofitClient;
import com.example.memeverseapp.ui.MessagesActivity;
import com.example.memeverseapp.ui.PostDetailFragment;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private Button btnMarkAllRead;

    private ApiService apiService;
    private NotificationAdapter adapter;

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        adapter = new NotificationAdapter(notifications, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllRead());

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        apiService.getLatestNotifications().enqueue(new Callback<NotificationsResponse>() {
            @Override
            public void onResponse(Call<NotificationsResponse> call, Response<NotificationsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();

                    if (response.body().getNotifications() != null) {
                        notifications.addAll(response.body().getNotifications());
                    }

                    adapter.notifyDataSetChanged();

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load notifications");
                }
            }

            @Override
            public void onFailure(Call<NotificationsResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void markAllRead() {
        apiService.markAllNotificationsRead().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "All notifications marked as read");
                    loadNotifications();
                } else {
                    ToastUtils.showError(requireActivity(), "Failed to mark as read");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error");
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        String link = notification.getLink();

        if (link == null || link.isEmpty()) {
            ToastUtils.showInfo(requireActivity(), "No link found");
            return;
        }

        if (link.contains("post")) {
            int postId = extractLastNumber(link);

            if (postId > 0) {
                PostDetailFragment fragment = PostDetailFragment.newInstance(postId);

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                ToastUtils.showError(requireActivity(), "Invalid post link");
            }

            return;
        }

        if (link.contains("profile") || link.contains("user")) {
            int userId = extractLastNumber(link);

            if (userId > 0) {
                ProfileFragment fragment = ProfileFragment.newInstance(userId);

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                ToastUtils.showError(requireActivity(), "Invalid profile link");
            }

            return;
        }

        if (link.contains("message") || link.contains("conversation")) {
            startActivity(new Intent(requireContext(), MessagesActivity.class));
            return;
        }

        ToastUtils.showInfo(requireActivity(), "Unknown notification link");
    }

    private int extractLastNumber(String text) {
        try {
            String numbers = text.replaceAll("[^0-9]+", " ").trim();

            if (numbers.isEmpty()) {
                return 0;
            }

            String[] parts = numbers.split(" ");
            return Integer.parseInt(parts[parts.length - 1]);

        } catch (Exception e) {
            return 0;
        }
    }
}