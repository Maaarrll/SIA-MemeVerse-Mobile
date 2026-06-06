package com.example.memeverseapp.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.adapters.ConversationAdapter;
import com.example.memeverseapp.adapters.MessageAdapter;
import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.Conversation;
import com.example.memeverseapp.models.ConversationsResponse;
import com.example.memeverseapp.models.Message;
import com.example.memeverseapp.models.MessagesResponse;
import com.example.memeverseapp.models.ReportBody;
import com.example.memeverseapp.models.SendMessageBody;
import com.example.memeverseapp.network.ApiService;
import com.example.memeverseapp.services.RetrofitClient;
import com.example.memeverseapp.ui.ProfileFragment;
import com.example.memeverseapp.utils.PreferencesManager;
import com.example.memeverseapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private ApiService apiService;
    private PreferencesManager prefManager;

    private View conversationListView;
    private View chatView;

    private RecyclerView rvConversations;
    private RecyclerView rvMessages;

    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private TextView tvChatWith;

    private ConversationAdapter conversationAdapter;
    private MessageAdapter messageAdapter;

    private final List<Conversation> conversationList = new ArrayList<>();
    private final List<Message> messageList = new ArrayList<>();

    private int currentChatWith = -1;
    private String currentChatName = "";

    private final Handler pollHandler = new Handler();
    private Runnable pollRunnable;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        prefManager = new PreferencesManager(requireContext());

        initViews(view);
        setupRecyclerViews();
        setupButtons();
        setupPolling();

        chatView.setVisibility(View.GONE);
        conversationListView.setVisibility(View.VISIBLE);

        loadConversations();

        return view;
    }

    private void initViews(View view) {
        conversationListView = view.findViewById(R.id.conversationListView);
        chatView = view.findViewById(R.id.chatView);

        rvConversations = view.findViewById(R.id.rvConversations);
        rvMessages = view.findViewById(R.id.rvMessages);

        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnBack = view.findViewById(R.id.btnBack);
        tvChatWith = view.findViewById(R.id.tvChatWith);
    }

    private void setupRecyclerViews() {
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        conversationAdapter = new ConversationAdapter(conversationList, this);
        rvConversations.setAdapter(conversationAdapter);

        LinearLayoutManager messageLayoutManager = new LinearLayoutManager(requireContext());
        messageLayoutManager.setStackFromEnd(true);

        rvMessages.setLayoutManager(messageLayoutManager);
        messageAdapter = new MessageAdapter(messageList, prefManager.getUserId());
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            chatView.setVisibility(View.GONE);
            conversationListView.setVisibility(View.VISIBLE);
            currentChatWith = -1;
            loadConversations();
        });

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void setupPolling() {
        pollRunnable = () -> {
            if (currentChatWith != -1) {
                loadMessages(currentChatWith);
            }

            pollHandler.postDelayed(pollRunnable, 3000);
        };

        pollHandler.postDelayed(pollRunnable, 3000);
    }

    private void loadConversations() {
        int currentUserId = prefManager.getUserId();

        apiService.getConversations(currentUserId).enqueue(new Callback<ConversationsResponse>() {
            @Override
            public void onResponse(Call<ConversationsResponse> call, Response<ConversationsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    conversationList.clear();

                    if (response.body().getConversations() != null) {
                        conversationList.addAll(response.body().getConversations());
                    }

                    conversationAdapter.notifyDataSetChanged();

                    if (conversationList.isEmpty()) {
                        ToastUtils.showInfo(requireActivity(), "No conversations yet");
                    }

                } else {
                    String error = "Failed to load conversations";

                    if (response.body() != null && response.body().getError() != null) {
                        error = response.body().getError();
                    }

                    ToastUtils.showError(requireActivity(), error);
                }
            }

            @Override
            public void onFailure(Call<ConversationsResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onOpenChat(Conversation conversation) {
        currentChatWith = conversation.getId();

        currentChatName = conversation.getNickname() != null && !conversation.getNickname().isEmpty()
                ? conversation.getNickname()
                : conversation.getUsername();

        tvChatWith.setText(currentChatName);

        conversationListView.setVisibility(View.GONE);
        chatView.setVisibility(View.VISIBLE);

        loadMessages(currentChatWith);
    }

    private void loadMessages(int otherUserId) {
        int currentUserId = prefManager.getUserId();

        apiService.getMessages(currentUserId, otherUserId).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    messageList.clear();

                    if (response.body().getMessages() != null) {
                        messageList.addAll(response.body().getMessages());
                    }

                    messageAdapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }

                } else {
                    if (currentChatWith != -1) {
                        ToastUtils.showError(requireActivity(), "Failed to load messages");
                    }
                }
            }

            @Override
            public void onFailure(Call<MessagesResponse> call, Throwable t) {
                // Silent fail to avoid annoying the user every 3 seconds
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();

        if (currentChatWith == -1) {
            ToastUtils.showError(requireActivity(), "Open a conversation first");
            return;
        }

        if (text.isEmpty()) {
            return;
        }

        etMessage.setText("");

        int currentUserId = prefManager.getUserId();
        SendMessageBody body = new SendMessageBody(currentChatWith, text);

        apiService.sendMessage(currentUserId, body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadMessages(currentChatWith);
                    loadConversations();
                } else {
                    ToastUtils.showError(requireActivity(), "Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onOpenMenu(Conversation conversation, View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);

        popupMenu.getMenu().add("View Profile");
        popupMenu.getMenu().add("Report User");
        popupMenu.getMenu().add("Delete Conversation");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.equals("View Profile")) {
                viewProfile(conversation.getId());
                return true;
            }

            if (title.equals("Report User")) {
                showReportDialog(conversation.getId());
                return true;
            }

            if (title.equals("Delete Conversation")) {
                showDeleteConversationDialog(conversation.getId());
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void viewProfile(int userId) {
        ProfileFragment fragment = ProfileFragment.newInstance(userId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.messages_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showReportDialog(int userId) {
        EditText input = new EditText(requireContext());
        input.setHint("Reason for report");

        new AlertDialog.Builder(requireContext())
                .setTitle("Report User")
                .setView(input)
                .setPositiveButton("Report", (dialog, which) -> {
                    String reason = input.getText().toString().trim();

                    if (!reason.isEmpty()) {
                        reportUser(userId, reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reportUser(int userId, String reason) {
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
                ToastUtils.showError(requireActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showDeleteConversationDialog(int userId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (dialog, which) -> deleteConversation(userId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteConversation(int otherUserId) {
        int currentUserId = prefManager.getUserId();

        apiService.deleteConversation(currentUserId, otherUserId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ToastUtils.showSuccess(requireActivity(), "Conversation deleted");

                    if (currentChatWith == otherUserId) {
                        chatView.setVisibility(View.GONE);
                        conversationListView.setVisibility(View.VISIBLE);
                        currentChatWith = -1;
                    }

                    loadConversations();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
}