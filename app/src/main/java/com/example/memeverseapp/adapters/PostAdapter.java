package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.network.RetrofitClient;
import com.example.memeverseapp.utils.TimeUtils;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> posts;
    private final OnVoteClickListener voteListener;
    private final OnCommentClickListener commentListener;
    private final OnProfileClickListener profileListener;

    public interface OnVoteClickListener {
        void onVote(int postId, String vote);
    }

    public interface OnCommentClickListener {
        void onCommentClick(int postId);
    }

    public interface OnProfileClickListener {
        void onProfileClick(int userId);
    }

    public PostAdapter(
            List<Post> posts,
            OnVoteClickListener voteListener,
            OnCommentClickListener commentListener,
            OnProfileClickListener profileListener
    ) {
        this.posts = posts;
        this.voteListener = voteListener;
        this.commentListener = commentListener;
        this.profileListener = profileListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);

        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar, ivPostImage;
        TextView tvUsername, tvTime, tvCategory, tvCaption, tvDescription, tvTags, tvVoteScore;
        ImageButton btnUpvote, btnDownvote, btnComments;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTags = itemView.findViewById(R.id.tvTags);
            tvVoteScore = itemView.findViewById(R.id.tvVoteScore);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnDownvote = itemView.findViewById(R.id.btnDownvote);
            btnComments = itemView.findViewById(R.id.btnComments);
        }

        void bind(Post post) {
            tvUsername.setText(post.getNickname() != null ? post.getNickname() : post.getUsername());
            tvTime.setText(TimeUtils.getTimeAgo(post.getCreated_at()));
            tvCategory.setText(post.getCategory_name());
            tvCaption.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            tvTags.setText(post.getTags() != null ? post.getTags() : "");
            tvVoteScore.setText(String.valueOf(post.getVote_score()));

            String avatarUrl = RetrofitClient.getFullUrl(post.getAvatar_url());
            String imageUrl = RetrofitClient.getFullUrl(post.getImage_path());

            if (avatarUrl != null) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            if (imageUrl != null) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .fitCenter()
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivPostImage);
            } else {
                ivPostImage.setImageResource(R.drawable.ic_placeholder);
            }

            int vote = post.getUser_vote();

            if (vote == 1) {
                btnUpvote.setImageResource(R.drawable.ic_arrow_up_active);
                btnUpvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primary));

                btnDownvote.setImageResource(R.drawable.ic_arrow_down);
                btnDownvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));

            } else if (vote == -1) {
                btnDownvote.setImageResource(R.drawable.ic_arrow_down_active);
                btnDownvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primary));

                btnUpvote.setImageResource(R.drawable.ic_arrow_up);
                btnUpvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));

            } else {
                btnUpvote.setImageResource(R.drawable.ic_arrow_up);
                btnUpvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));

                btnDownvote.setImageResource(R.drawable.ic_arrow_down);
                btnDownvote.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
            }

            btnUpvote.setOnClickListener(v -> voteListener.onVote(post.getId(), "up"));
            btnDownvote.setOnClickListener(v -> voteListener.onVote(post.getId(), "down"));
            btnComments.setOnClickListener(v -> commentListener.onCommentClick(post.getId()));

            ivAvatar.setOnClickListener(v -> profileListener.onProfileClick(post.getUser_id()));
            tvUsername.setOnClickListener(v -> profileListener.onProfileClick(post.getUser_id()));
        }
    }
}