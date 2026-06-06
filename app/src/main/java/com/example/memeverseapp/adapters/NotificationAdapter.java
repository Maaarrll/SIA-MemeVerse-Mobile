package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.AppNotification;
import com.example.memeverseapp.utils.TimeUtils;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<AppNotification> notifications;

    public NotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder,
            int position
    ) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView tvIcon, tvTitle, tvMessage, tvTime, tvUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadDot = itemView.findViewById(R.id.tvUnreadDot);
        }

        void bind(AppNotification notification) {
            String title = notification.getTitle();

            if (title == null || title.trim().isEmpty()) {
                title = "MemeVerse";
            }

            String message = notification.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = "You have a new notification";
            }

            tvTitle.setText(title);
            tvMessage.setText(message);

            if (notification.getCreated_at() != null && !notification.getCreated_at().isEmpty()) {
                tvTime.setText(TimeUtils.getTimeAgo(notification.getCreated_at()));
            } else {
                tvTime.setText("");
            }

            tvIcon.setText(getIcon(notification.getType()));

            if (notification.getIs_read() == 0) {
                tvUnreadDot.setVisibility(View.VISIBLE);
            } else {
                tvUnreadDot.setVisibility(View.GONE);
            }
        }

        private String getIcon(String type) {
            if (type == null) return "🔔";

            switch (type) {
                case "comment":
                    return "💬";
                case "vote":
                    return "⬆";
                case "message":
                    return "✉";
                case "follow":
                    return "👤";
                default:
                    return "🔔";
            }
        }
    }
}