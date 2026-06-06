package com.example.memeverseapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.PostAdapter;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.models.PostsResponse;
import com.example.memeverseapp.models.VoteBody;
import com.example.memeverseapp.models.VoteResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.ui.PostDetailFragment;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment implements
        PostAdapter.OnVoteClickListener,
        PostAdapter.OnCommentClickListener,
        PostAdapter.OnProfileClickListener {

    private static final String ARG_SLUG = "slug";

    private String slug;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ApiService apiService;
    private PostAdapter adapter;

    private final List<Post> posts = new ArrayList<>();

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    public static CategoryFragment newInstance(String slug) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SLUG, slug);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        if (getArguments() != null) {
            slug = getArguments().getString(ARG_SLUG);
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        adapter = new PostAdapter(posts, this, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupScrollListener();
        loadCategoryPosts(true);

        return view;
    }

    private void setupScrollListener() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    @NonNull RecyclerView recyclerView,
                    int dx,
                    int dy
            ) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                            && firstVisibleItemPosition >= 0) {
                        loadCategoryPosts(false);
                    }
                }
            }
        });
    }

    private void loadCategoryPosts(boolean refresh) {
        if (slug == null || slug.isEmpty()) {
            ToastUtils.showError(requireActivity(), "No category selected");
            return;
        }

        if (isLoading) return;

        isLoading = true;

        if (refresh) {
            currentPage = 1;
            hasMore = true;
            posts.clear();
            adapter.notifyDataSetChanged();
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.getPostsByCategory(currentPage, 10, slug).enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PostsResponse postsResponse = response.body();
                    List<Post> newPosts = postsResponse.getPosts();

                    if (newPosts == null || newPosts.isEmpty()) {
                        hasMore = false;

                        if (posts.isEmpty()) {
                            ToastUtils.showInfo(requireActivity(), "No posts in this category");
                        }

                    } else {
                        posts.addAll(newPosts);
                        adapter.notifyDataSetChanged();
                        currentPage++;

                        if (postsResponse.getPagination() != null) {
                            hasMore = postsResponse.getPagination().isHas_more();
                        }
                    }

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load category");
                }
            }

            @Override
            public void onFailure(Call<PostsResponse> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onVote(int postId, String vote) {
        int voteValue;

        if ("up".equals(vote)) {
            voteValue = 1;
        } else if ("down".equals(vote)) {
            voteValue = -1;
        } else {
            voteValue = 0;
        }

        apiService.vote(new VoteBody(postId, voteValue)).enqueue(new Callback<VoteResponse>() {
            @Override
            public void onResponse(Call<VoteResponse> call, Response<VoteResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadCategoryPosts(true);
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

    @Override
    public void onCommentClick(int postId) {
        PostDetailFragment fragment = PostDetailFragment.newInstance(postId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfileClick(int userId) {
        ProfileFragment fragment = ProfileFragment.newInstance(userId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}