package com.example.memeverseapp.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Notification;
import com.example.memeverseapp.utils.TimeUtils;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        LinearLayout notificationContainer;
        TextView tvNotificationIcon, tvNotificationMessage, tvNotificationTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationContainer = itemView.findViewById(R.id.notificationContainer);
            tvNotificationIcon = itemView.findViewById(R.id.tvNotificationIcon);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
        }

        void bind(Notification notification) {
            tvNotificationMessage.setText(notification.getMessage());

            if (notification.getTime() != null && !notification.getTime().isEmpty()) {
                tvNotificationTime.setText(notification.getTime());
            } else {
                tvNotificationTime.setText(TimeUtils.getTimeAgo(notification.getCreated_at()));
            }

            if (notification.getIcon() != null && !notification.getIcon().isEmpty()) {
                tvNotificationIcon.setText(notification.getIcon());
            } else {
                tvNotificationIcon.setText("🔔");
            }

            if (!notification.isIs_read()) {
                tvNotificationMessage.setTypeface(null, Typeface.BOLD);
            } else {
                tvNotificationMessage.setTypeface(null, Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }
    }
}