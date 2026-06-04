package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Message;
import com.example.memeverseapp.utils.TimeUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int TYPE_OUTGOING = 1;
    private static final int TYPE_INCOMING = 2;

    private final List<Message> messages;
    private final int myUserId;

    public MessageAdapter(List<Message> messages, int myUserId) {
        this.messages = messages;
        this.myUserId = myUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.getSender_id() == myUserId) {
            return TYPE_OUTGOING;
        } else {
            return TYPE_INCOMING;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;

        if (viewType == TYPE_OUTGOING) {
            layout = R.layout.item_message_outgoing;
        } else {
            layout = R.layout.item_message_incoming;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessageText, tvMessageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(Message message) {
            tvMessageText.setText(message.getMessage());
            tvMessageTime.setText(TimeUtils.getTimeAgo(message.getCreated_at()));
        }
    }
}