package com.example.memeverseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memeverseapp.R;
import com.example.memeverseapp.models.Comment;
import com.example.memeverseapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> displayList = new ArrayList<>();
    private final HashMap<Integer, Integer> depthMap = new HashMap<>();
    private final OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onReply(Comment comment);
        void onDelete(Comment comment);
    }

    public CommentAdapter(List<Comment> comments, OnCommentActionListener listener) {
        this.listener = listener;
        buildNestedList(comments);
    }

    public void updateComments(List<Comment> comments) {
        buildNestedList(comments);
        notifyDataSetChanged();
    }

    private void buildNestedList(List<Comment> comments) {
        displayList.clear();
        depthMap.clear();

        for (Comment comment : comments) {
            if (comment.getParent_id() == null || comment.getParent_id() == 0) {
                addCommentAndReplies(comment, comments, 0);
            }
        }
    }

    private void addCommentAndReplies(Comment parent, List<Comment> allComments, int depth) {
        displayList.add(parent);
        depthMap.put(parent.getId(), depth);

        for (Comment child : allComments) {
            if (child.getParent_id() != null && child.getParent_id() == parent.getId()) {
                addCommentAndReplies(child, allComments, depth + 1);
            }
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(displayList.get(position));
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout commentContainer;
        TextView tvCommentUser, tvCommentTime, tvCommentText;
        Button btnReply, btnDeleteComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            commentContainer = itemView.findViewById(R.id.commentContainer);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }

        void bind(Comment comment) {
            tvCommentUser.setText(comment.getNickname() != null ? comment.getNickname() : comment.getUsername());
            tvCommentTime.setText(TimeUtils.getTimeAgo(comment.getCreated_at()));
            tvCommentText.setText(comment.getComment_text());

            int depth = depthMap.containsKey(comment.getId()) ? depthMap.get(comment.getId()) : 0;

            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) commentContainer.getLayoutParams();

            params.leftMargin = depth * 40;
            commentContainer.setLayoutParams(params);

            btnReply.setOnClickListener(v -> listener.onReply(comment));
            btnDeleteComment.setOnClickListener(v -> listener.onDelete(comment));
        }
    }
}