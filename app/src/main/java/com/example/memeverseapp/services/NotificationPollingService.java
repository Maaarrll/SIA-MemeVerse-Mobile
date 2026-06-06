package com.example.memeverseapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.UnreadCountResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationPollingService extends Service {

    private static final String TAG = "PollingService";
    private static final long POLLING_INTERVAL = 5000;
    private static final String CHANNEL_ID = "memeverse_polling_channel";

    private Handler handler;
    private Runnable pollingRunnable;
    private ApiService apiService;

    private static final MutableLiveData<Integer> unreadNotificationsCount = new MutableLiveData<>(0);
    private static final MutableLiveData<Integer> unreadMessagesCount = new MutableLiveData<>(0);

    public static LiveData<Integer> getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public static LiveData<Integer> getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        apiService = RetrofitClient.getClient().create(ApiService.class);

        createNotificationChannel();
        startForeground(1, createForegroundNotification());

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                pollUnreadCounts();
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };

        handler.post(pollingRunnable);
    }

    private android.app.Notification createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MemeVerse")
                .setContentText("Checking notifications...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "MemeVerse Polling",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void pollUnreadCounts() {
        apiService.getUnreadNotifications().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    unreadNotificationsCount.postValue(response.body().getCount());
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e(TAG, "Unread notifications failed: " + t.getMessage());
            }
        });

        apiService.getUnreadMessages().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    unreadMessagesCount.postValue(response.body().getCount());
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e(TAG, "Unread messages failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (handler != null && pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}