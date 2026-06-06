package com.example.memeverseapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {

    private int id;
    private int user_id;
    private String title;
    private String description;
    private String tags;
    private String image_path;
    private String category_name;
    private String category_slug;
    private int vote_score;
    private int comment_count;
    private int user_vote;
    private String username;
    private String nickname;
    private String avatar_url;
    private String created_at;
    private int category_id;

    // IMPORTANT:
    // Web API may return "comments" as a number.
    // So Android should not map "comments" into List<Comment>.
    @SerializedName("comments_list")
    private List<Comment> comments;

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public String getImage_path() {
        return image_path;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_slug() {
        return category_slug;
    }

    public int getVote_score() {
        return vote_score;
    }

    public int getComment_count() {
        return comment_count;
    }

    public int getUser_vote() {
        return user_vote;
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

    public String getCreated_at() {
        return created_at;
    }

    public int getCategory_id() {
        return category_id;
    }

    public List<Comment> getComments() {
        return comments;
    }
}