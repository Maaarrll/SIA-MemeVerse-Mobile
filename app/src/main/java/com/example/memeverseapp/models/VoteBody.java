package com.example.memeverseapp.models;

public class VoteBody {
    private int post_id;
    private int vote;

    public VoteBody(int post_id, int vote) {
        this.post_id = post_id;
        this.vote = vote;
    }

    public int getPost_id() {
        return post_id;
    }

    public int getVote() {
        return vote;
    }
}