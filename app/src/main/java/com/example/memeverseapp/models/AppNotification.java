package com.example.memeverseapp.models;

public class AppNotification {

    private int id;
    private int user_id;
    private String type;
    private String title;
    private String message;
    private int related_id;
    private int is_read;
    private String created_at;

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getRelated_id() {
        return related_id;
    }

    public int getIs_read() {
        return is_read;
    }

    public String getCreated_at() {
        return created_at;
    }
}