package com.example.memeverseapp.models;

import java.util.List;

public class ConversationsResponse {

    private boolean success;
    private String error;
    private List<Conversation> conversations;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }
}