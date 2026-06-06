package com.example.memeverseapp.models;

import java.util.List;

public class MessagesResponse {

    private boolean success;
    private String error;
    private List<Message> messages;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<Message> getMessages() {
        return messages;
    }
}