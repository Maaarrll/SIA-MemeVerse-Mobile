package com.example.memeverseapp.models;

public class Conversation {

    private int user_id;
    private String username;
    private String nickname;
    private String avatar_url;
    private String last_message;
    private String last_time;
    private int unread_count;

    public int getUser_id() {
        return user_id;
    }

    // Compatibility for MessagesFragment
    public int getId() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getLast_message() {
        return last_message;
    }

    public String getLast_time() {
        return last_time;
    }

    public int getUnread_count() {
        return unread_count;
    }

    // Compatibility for ConversationAdapter
    public String getLast_msg() {
        return last_message;
    }

    public int getUnread() {
        return unread_count;
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }

        if (username != null && !username.trim().isEmpty()) {
            return username;
        }

        return "Unknown User";
    }
}