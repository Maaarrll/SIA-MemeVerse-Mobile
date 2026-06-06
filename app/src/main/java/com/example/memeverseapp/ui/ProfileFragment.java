package com.example.memeverseapp.ui;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.ProfilePostsAdapter;
import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.models.ReportBody;
import com.example.memeverseapp.models.User;
import com.example.memeverseapp.models.UserPostsResponse;
import com.example.memeverseapp.models.UserResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.TimeUtils;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements ProfilePostsAdapter.OnProfilePostClickListener {

    private static final String ARG_USER_ID = "user_id";

    private int userId;
    private boolean isFollowing = false;

    private ApiService apiService;
    private PreferencesManager prefManager;

    private ImageView ivProfileAvatar;
    private TextView tvNickname, tvUsername, tvBio, tvJoinDate, tvPostCount;
    private Button btnFollow, btnReport, btnEditProfile;
    private LinearLayout actionButtons;
    private RecyclerView rvProfilePosts;

    private User currentUser;
    private ProfilePostsAdapter adapter;
    private final List<Post> userPosts = new ArrayList<>();

    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID);
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
        prefManager = new PreferencesManager(requireContext());

        if (userId == 0) {
            userId = prefManager.getUserId();
        }

        initViews(view);
        setupRecyclerView();
        setupButtons();

        loadUser();
        loadUserPosts();

        return view;
    }

    private void initViews(View view) {
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvNickname = view.findViewById(R.id.tvNickname);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBio = view.findViewById(R.id.tvBio);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvPostCount = view.findViewById(R.id.tvPostCount);

        btnFollow = view.findViewById(R.id.btnFollow);
        btnReport = view.findViewById(R.id.btnReport);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        actionButtons = view.findViewById(R.id.actionButtons);

        rvProfilePosts = view.findViewById(R.id.rvProfilePosts);
    }

    private void setupRecyclerView() {
        rvProfilePosts.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new ProfilePostsAdapter(userPosts, this);
        rvProfilePosts.setAdapter(adapter);
    }

    private void setupButtons() {
        btnFollow.setOnClickListener(v -> toggleFollow());
        btnReport.setOnClickListener(v -> showReportDialog());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void loadUser() {
        apiService.getUser(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getUser();
                    displayUser();
                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load profile");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void displayUser() {
        if (currentUser == null) return;

        tvNickname.setText(currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getUsername());
        tvUsername.setText("@" + currentUser.getUsername());
        tvBio.setText(currentUser.getBio() != null && !currentUser.getBio().isEmpty() ? currentUser.getBio() : "No bio yet");
        tvJoinDate.setText("Joined " + TimeUtils.formatJoinDate(currentUser.getCreated_at()));

        String avatarUrl = RetrofitClient.getFullUrl(currentUser.getAvatar());

        if (avatarUrl != null) {
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        if (userId == prefManager.getUserId()) {
            actionButtons.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.VISIBLE);
        } else {
            actionButtons.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
        }
    }

    private void loadUserPosts() {
        apiService.getUserPosts(userId).enqueue(new Callback<UserPostsResponse>() {
            @Override
            public void onResponse(Call<UserPostsResponse> call, Response<UserPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userPosts.clear();

                    if (response.body().getPosts() != null) {
                        userPosts.addAll(response.body().getPosts());
                    }

                    tvPostCount.setText(userPosts.size() + " posts");
                    adapter.notifyDataSetChanged();

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load user posts");
                }
            }

            @Override
            public void onFailure(Call<UserPostsResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void toggleFollow() {
        String action;

        if (isFollowing) {
            action = "unfollow";
        } else {
            action = "follow";
        }

        apiService.follow(userId, action).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    isFollowing = !isFollowing;

                    if (isFollowing) {
                        btnFollow.setText("Unfollow");
                    } else {
                        btnFollow.setText("Follow");
                    }

                    ToastUtils.showSuccess(requireActivity(), "Updated");
                } else {
                    ToastUtils.showError(requireActivity(), "Action failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error");
            }
        });
    }

    private void showReportDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Reason for report");

        new AlertDialog.Builder(requireContext())
                .setTitle("Report User")
                .setView(input)
                .setPositiveButton("Report", (dialog, which) -> {
                    String reason = input.getText().toString().trim();

                    if (!reason.isEmpty()) {
                        reportUser(reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reportUser(String reason) {
        ReportBody body = new ReportBody("user", userId, reason);

        apiService.report(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "User reported");
                } else {
                    ToastUtils.showError(requireActivity(), "Report failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error");
            }
        });
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);

        EditText nicknameInput = new EditText(requireContext());
        nicknameInput.setHint("Nickname");
        nicknameInput.setText(currentUser.getNickname());

        EditText bioInput = new EditText(requireContext());
        bioInput.setHint("Bio");
        bioInput.setText(currentUser.getBio());

        layout.addView(nicknameInput);
        layout.addView(bioInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String nickname = nicknameInput.getText().toString().trim();
                    String bio = bioInput.getText().toString().trim();

                    updateProfile(nickname, bio);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfile(String nickname, String bio) {
        MultipartBody.Part avatar = null;

        apiService.updateProfile(nickname, bio, avatar).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "Profile updated");
                    loadUser();
                } else {
                    ToastUtils.showError(requireActivity(), "Update failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error");
            }
        });
    }

    @Override
    public void onPostClick(Post post) {
        PostDetailFragment fragment = PostDetailFragment.newInstance(post.getId());

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPostLongClick(Post post) {
        if (userId != prefManager.getUserId()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Your Post")
                .setItems(new String[]{"Open Post"}, (dialog, which) -> {
                    if (which == 0) {
                        onPostClick(post);
                    }
                })
                .show();
    }
}