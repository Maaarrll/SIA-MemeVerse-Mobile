package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Conversation;
import com.example.memeverseapp.services.RetrofitClient;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onOpenChat(Conversation conversation);
        void onOpenMenu(Conversation conversation, View anchor);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);

        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(conversations.get(position));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView ivConversationAvatar;
        TextView tvConversationName, tvLastMessage, tvUnreadBadge;
        ImageButton btnConversationMenu;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            ivConversationAvatar = itemView.findViewById(R.id.ivConversationAvatar);
            tvConversationName = itemView.findViewById(R.id.tvConversationName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            btnConversationMenu = itemView.findViewById(R.id.btnConversationMenu);
        }

        void bind(Conversation conversation) {
            tvConversationName.setText(
                    conversation.getNickname() != null && !conversation.getNickname().isEmpty()
                            ? conversation.getNickname()
                            : conversation.getUsername()
            );

            tvLastMessage.setText(
                    conversation.getLast_msg() != null
                            ? conversation.getLast_msg()
                            : ""
            );

            if (conversation.getUnread() > 0) {
                tvUnreadBadge.setVisibility(View.VISIBLE);
                tvUnreadBadge.setText(String.valueOf(conversation.getUnread()));
            } else {
                tvUnreadBadge.setVisibility(View.GONE);
            }

            String avatarUrl = RetrofitClient.getFullUrl(conversation.getAvatar_url());

            if (avatarUrl != null) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(ivConversationAvatar);
            } else {
                ivConversationAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            itemView.setOnClickListener(v -> listener.onOpenChat(conversation));
            btnConversationMenu.setOnClickListener(v -> listener.onOpenMenu(conversation, btnConversationMenu));
        }
    }
}