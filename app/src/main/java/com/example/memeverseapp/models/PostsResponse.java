package com.example.memeverseapp.models;

import java.util.List;


public class PostsResponse {

    private List<Post> posts;
    private Pagination pagination;

    public List<Post> getPosts() {
        return posts;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public static class Pagination {
        private int current_page;
        private int total_pages;
        private int total_posts;
        private boolean has_more;

        public int getCurrent_page() {
            return current_page;
        }

        public int getTotal_pages() {
            return total_pages;
        }

        public int getTotal_posts() {
            return total_posts;
        }

        public boolean isHas_more() {
            return has_more;
        }
    }
}