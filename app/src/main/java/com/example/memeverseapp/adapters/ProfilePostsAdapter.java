package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.services.RetrofitClient;

import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostViewHolder> {

    private final List<Post> posts;
    private final OnProfilePostClickListener listener;

    public interface OnProfilePostClickListener {
        void onPostClick(Post post);
        void onPostLongClick(Post post);
    }

    public ProfilePostsAdapter(List<Post> posts, OnProfilePostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_post, parent, false);

        return new ProfilePostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ProfilePostViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfilePost;

        public ProfilePostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePost = itemView.findViewById(R.id.ivProfilePost);
        }

        void bind(Post post) {
            String imageUrl = RetrofitClient.getFullUrl(post.getImage_path());

            if (imageUrl != null) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivProfilePost);
            } else {
                ivProfilePost.setImageResource(R.drawable.ic_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onPostClick(post));

            itemView.setOnLongClickListener(v -> {
                listener.onPostLongClick(post);
                return true;
            });
        }
    }
}