package com.example.memeverseapp.ui.fragments;

import com.example.memeverseapp.models.PostsResponse;
import com.example.memeverseapp.models.VoteBody;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.PostAdapter;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.models.VoteResponse;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.ui.PostDetailFragment;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements
        PostAdapter.OnVoteClickListener,
        PostAdapter.OnCommentClickListener,
        PostAdapter.OnProfileClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;

    private PostAdapter adapter;
    private final List<Post> posts = new ArrayList<>();

    private ApiService apiService;
    private PreferencesManager prefManager;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        prefManager = new PreferencesManager(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();

        loadPosts(true);

        return view;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PostAdapter(posts, this, this, this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    @NonNull RecyclerView recyclerView,
                    int dx,
                    int dy
            ) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                            && firstVisibleItemPosition >= 0) {
                        loadPosts(false);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> loadPosts(true));
    }

    private void loadPosts(boolean refresh) {
        if (isLoading) return;

        isLoading = true;

        if (refresh) {
            currentPage = 1;
            hasMore = true;
            posts.clear();
            adapter.notifyDataSetChanged();
        }

        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getPosts(currentPage, 10).enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    PostsResponse postsResponse = response.body();
                    List<Post> newPosts = postsResponse.getPosts();

                    if (newPosts == null || newPosts.isEmpty()) {
                        hasMore = false;
                    } else {
                        posts.addAll(newPosts);
                        adapter.notifyDataSetChanged();
                        currentPage++;

                        if (postsResponse.getPagination() != null) {
                            hasMore = postsResponse.getPagination().isHas_more();
                        }
                    }

                } else {
                    ToastUtils.showError(requireActivity(), "Failed to load posts");
                }
            }

            @Override
            public void onFailure(Call<PostsResponse> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

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
                    loadPosts(true);
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