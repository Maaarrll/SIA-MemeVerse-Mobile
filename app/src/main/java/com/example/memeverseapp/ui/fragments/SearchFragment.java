package com.example.memeverseapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Category;
import com.example.memeverseapp.models.Post;
import com.example.memeverseapp.models.SearchResponse;
import com.example.memeverseapp.models.User;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.ui.PostDetailFragment;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.TimeUtils;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SearchAdapter adapter;

    private final List<Object> results = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        adapter = new SearchAdapter(results);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void performSearch(String query) {
        if (query == null || query.length() < 2) {
            results.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.search(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    results.clear();

                    SearchResponse data = response.body();

                    if (data.getPosts() != null && !data.getPosts().isEmpty()) {
                        results.add("POSTS");
                        results.addAll(data.getPosts());
                    }

                    if (data.getUsers() != null && !data.getUsers().isEmpty()) {
                        results.add("USERS");
                        results.addAll(data.getUsers());
                    }

                    if (data.getCategories() != null && !data.getCategories().isEmpty()) {
                        results.add("CATEGORIES");
                        results.addAll(data.getCategories());
                    }

                    if (results.isEmpty()) {
                        ToastUtils.showInfo(requireActivity(), "No results found");
                    }

                    adapter.notifyDataSetChanged();

                } else {
                    ToastUtils.showError(requireActivity(), "Search failed");
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_POST = 1;
        private static final int TYPE_USER = 2;
        private static final int TYPE_CATEGORY = 3;

        private final List<Object> items;

        SearchAdapter(List<Object> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            Object item = items.get(position);

            if (item instanceof String) return TYPE_HEADER;
            if (item instanceof Post) return TYPE_POST;
            if (item instanceof User) return TYPE_USER;
            if (item instanceof Category) return TYPE_CATEGORY;

            return TYPE_HEADER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            if (viewType == TYPE_HEADER) {
                return new HeaderViewHolder(inflater.inflate(R.layout.item_search_header, parent, false));
            } else if (viewType == TYPE_POST) {
                return new PostViewHolder(inflater.inflate(R.layout.item_post_small, parent, false));
            } else if (viewType == TYPE_USER) {
                return new UserViewHolder(inflater.inflate(R.layout.item_user, parent, false));
            } else {
                return new CategoryViewHolder(inflater.inflate(R.layout.item_category, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);

            if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).bind((String) item);
            } else if (holder instanceof PostViewHolder) {
                ((PostViewHolder) holder).bind((Post) item);
            } else if (holder instanceof UserViewHolder) {
                ((UserViewHolder) holder).bind((User) item);
            } else if (holder instanceof CategoryViewHolder) {
                ((CategoryViewHolder) holder).bind((Category) item);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView tvHeader;

            HeaderViewHolder(View view) {
                super(view);
                tvHeader = view.findViewById(R.id.tvHeader);
            }

            void bind(String text) {
                tvHeader.setText(text);
            }
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivImage;
            TextView tvTitle, tvUsername, tvMeta;

            PostViewHolder(View view) {
                super(view);
                ivImage = view.findViewById(R.id.ivImage);
                tvTitle = view.findViewById(R.id.tvTitle);
                tvUsername = view.findViewById(R.id.tvUsername);
                tvMeta = view.findViewById(R.id.tvMeta);
            }

            void bind(Post post) {
                tvTitle.setText(post.getTitle() != null ? post.getTitle() : "Untitled");
                tvUsername.setText(post.getNickname() != null ? post.getNickname() : post.getUsername());
                tvMeta.setText(TimeUtils.getTimeAgo(post.getCreated_at()));

                String imageUrl = RetrofitClient.getFullUrl(post.getImage_path());

                if (imageUrl != null) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.ic_placeholder);
                }

                itemView.setOnClickListener(v -> {
                    PostDetailFragment fragment = PostDetailFragment.newInstance(post.getId());

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivAvatar;
            TextView tvUsername;

            UserViewHolder(View view) {
                super(view);
                ivAvatar = view.findViewById(R.id.ivAvatar);
                tvUsername = view.findViewById(R.id.tvUsername);
            }

            void bind(User user) {
                tvUsername.setText(user.getNickname() != null ? user.getNickname() : user.getUsername());

                String avatarUrl = RetrofitClient.getFullUrl(user.getAvatar());

                if (avatarUrl != null) {
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }

                itemView.setOnClickListener(v -> {
                    ProfileFragment fragment = ProfileFragment.newInstance(user.getId());

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;

            CategoryViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
            }

            void bind(Category category) {
                tvName.setText("# " + category.getName());

                itemView.setOnClickListener(v -> {
                    CategoryFragment fragment = CategoryFragment.newInstance(category.getSlug());

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }
    }
}