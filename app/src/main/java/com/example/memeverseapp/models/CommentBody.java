package com.example.memeverseapp.models;

public class CommentBody {
    private int post_id;
    private String content;

    public CommentBody(int post_id, String content) {
        this.post_id = post_id;
        this.content = content;
    }

    public int getPost_id() {
        return post_id;
    }

    public String getContent() {
        return content;
    }
}