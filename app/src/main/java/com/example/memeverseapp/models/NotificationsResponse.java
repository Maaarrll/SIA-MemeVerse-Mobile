package com.example.memeverseapp.models;

import java.util.List;

public class NotificationsResponse {

    private boolean success;
    private String error;
    private List<AppNotification> notifications;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<AppNotification> getNotifications() {
        return notifications;
    }
}