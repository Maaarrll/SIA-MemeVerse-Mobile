package com.example.memeverseapp.ui;

import com.example.memeverseapp.models.VoteBody;
import com.example.memeverseapp.models.CommentBody;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.CommentAdapter;
import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.Comment;
import com.example.memeverseapp.models.CommentDeleteBody;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.models.PostDetailResponse;
import com.example.memeverseapp.models.VoteResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.TimeUtils;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailFragment extends Fragment implements CommentAdapter.OnCommentActionListener {

    private static final String ARG_POST_ID = "post_id";

    private int postId;
    private Post post;

    private ApiService apiService;
    private PreferencesManager prefManager;

    private ImageView ivImage, ivAvatar;
    private TextView tvUsername, tvTime, tvCategory, tvTitle, tvDescription, tvVoteScore;
    private Button btnUpvote, btnDownvote, btnEdit, btnDelete, btnPostComment;
    private EditText etComment;
    private RecyclerView rvComments;
    private LinearLayout ownerActions;

    private CommentAdapter adapter;
    private final List<Comment> commentList = new ArrayList<>();

    public static PostDetailFragment newInstance(int postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_post_detail, container, false);

        if (getArguments() != null) {
            postId = getArguments().getInt(ARG_POST_ID);
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
        prefManager = new PreferencesManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupClicks();

        loadPost();

        return view;
    }

    private void initViews(View view) {
        ivImage = view.findViewById(R.id.ivImage);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvTime = view.findViewById(R.id.tvTime);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvVoteScore = view.findViewById(R.id.tvVoteScore);

        btnUpvote = view.findViewById(R.id.btnUpvote);
        btnDownvote = view.findViewById(R.id.btnDownvote);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnPostComment = view.findViewById(R.id.btnPostComment);

        etComment = view.findViewById(R.id.etComment);
        rvComments = view.findViewById(R.id.rvComments);
        ownerActions = view.findViewById(R.id.ownerActions);
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter(commentList, this);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(adapter);
    }

    private void setupClicks() {
        btnUpvote.setOnClickListener(v -> votePost("up"));
        btnDownvote.setOnClickListener(v -> votePost("down"));

        btnPostComment.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();

            if (text.isEmpty()) {
                ToastUtils.showError(requireActivity(), "Write a comment first");
                return;
            }

            addComment(text);
        });

        btnEdit.setOnClickListener(v -> showEditPostDialog());
        btnDelete.setOnClickListener(v -> showDeletePostDialog());
    }

    private void loadPost() {
        apiService.getPost(postId).enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    post = response.body().getPost();

                    if (post != null) {
                        displayPost();
                    } else {
                        ToastUtils.showError(requireActivity(), "Post not found");
                    }

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load post");
                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void displayPost() {
        if (post == null) return;

        String displayName = post.getNickname();

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = post.getUsername();
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Unknown User";
        }

        tvUsername.setText(displayName);

        if (post.getCreated_at() != null && !post.getCreated_at().isEmpty()) {
            tvTime.setText(TimeUtils.getTimeAgo(post.getCreated_at()));
        } else {
            tvTime.setText("");
        }

        tvCategory.setText(post.getCategory_name() != null ? post.getCategory_name() : "No Category");
        tvTitle.setText(post.getTitle() != null ? post.getTitle() : "");
        tvDescription.setText(post.getDescription() != null ? post.getDescription() : "");
        tvVoteScore.setText(String.valueOf(post.getVote_score()));

        String imageUrl = RetrofitClient.getFullUrl(post.getImage_path());
        String avatarUrl = RetrofitClient.getFullUrl(post.getAvatar_url());

        if (imageUrl != null) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.ic_placeholder);
        }

        if (avatarUrl != null) {
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        if (post.getUser_id() == prefManager.getUserId()) {
            ownerActions.setVisibility(View.VISIBLE);
        } else {
            ownerActions.setVisibility(View.GONE);
        }

        commentList.clear();

        if (post.getComments() != null) {
            commentList.addAll(post.getComments());
        }

        adapter.updateComments(commentList);
    }

    private void votePost(String voteType) {
        int voteValue;

        if ("up".equals(voteType)) {
            voteValue = 1;
        } else if ("down".equals(voteType)) {
            voteValue = -1;
        } else {
            voteValue = 0;
        }

        apiService.vote(new VoteBody(postId, voteValue)).enqueue(new Callback<VoteResponse>() {
            @Override
            public void onResponse(Call<VoteResponse> call, Response<VoteResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadPost();
                } else {
                    ToastUtils.showError(requireActivity(), "Vote failed");
                }
            }

            @Override
            public void onFailure(Call<VoteResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void addComment(String text) {
        apiService.addComment(new CommentBody(postId, text)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    etComment.setText("");
                    ToastUtils.showSuccess(requireActivity(), "Comment added");
                    loadPost();
                } else {
                    ToastUtils.showError(requireActivity(), "Failed to comment");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onReply(Comment comment) {
        EditText input = new EditText(requireContext());
        input.setHint("Write your reply...");

        new AlertDialog.Builder(requireContext())
                .setTitle("Reply")
                .setView(input)
                .setPositiveButton("Post", (dialog, which) -> {
                    String reply = input.getText().toString().trim();

                    if (!reply.isEmpty()) {
                        addComment(reply);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(Comment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> deleteComment(comment.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(int commentId) {
        apiService.deleteComment(new CommentDeleteBody(commentId)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "Comment deleted");
                    loadPost();
                } else {
                    ToastUtils.showError(requireActivity(), "Delete failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showEditPostDialog() {
        if (post == null) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);

        EditText titleInput = new EditText(requireContext());
        titleInput.setHint("Title");
        titleInput.setText(post.getTitle());

        EditText descInput = new EditText(requireContext());
        descInput.setHint("Description");
        descInput.setText(post.getDescription());

        layout.addView(titleInput);
        layout.addView(descInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Post")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String desc = descInput.getText().toString().trim();

                    editPost(title, desc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editPost(String title, String desc) {
        int categoryId = post != null ? post.getCategory_id() : 0;

        apiService.editPost(postId, title, desc, categoryId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "Post updated");
                    loadPost();
                } else {
                    ToastUtils.showError(requireActivity(), "Update failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showDeletePostDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost() {
        apiService.deletePost(postId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "Post deleted");
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    ToastUtils.showError(requireActivity(), "Delete failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }
}