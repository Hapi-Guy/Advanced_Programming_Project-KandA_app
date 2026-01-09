package com.kna.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.kna.android.R;
import com.kna.android.data.model.AnswerWithUser;

import java.util.ArrayList;
import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {
    
    private List<AnswerWithUser> answers = new ArrayList<>();
    private long currentUserId;
    private long questionOwnerId;
    private OnAnswerActionListener listener;
    
    public interface OnAnswerActionListener {
        void onUpvote(long answerId);
        void onDownvote(long answerId);
        void onAcceptAnswer(long answerId);
    }
    
    public AnswerAdapter(long currentUserId, long questionOwnerId, OnAnswerActionListener listener) {
        this.currentUserId = currentUserId;
        this.questionOwnerId = questionOwnerId;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_answer, parent, false);
        return new AnswerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        AnswerWithUser answerWithUser = answers.get(position);
        holder.bind(answerWithUser);
    }
    
    @Override
    public int getItemCount() {
        return answers.size();
    }
    
    public void setAnswers(List<AnswerWithUser> answers) {
        this.answers = answers;
        notifyDataSetChanged();
    }
    
    class AnswerViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView answerUserName;
        private final TextView answerUserDept;
        private final TextView answerContent;
        private final MaterialButton btnUpvote;
        private final MaterialButton btnDownvote;
        private final MaterialButton btnAcceptAnswer;
        private final TextView acceptedBadge;
        
        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            answerUserName = itemView.findViewById(R.id.answerUserName);
            answerUserDept = itemView.findViewById(R.id.answerUserDept);
            answerContent = itemView.findViewById(R.id.answerContent);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnDownvote = itemView.findViewById(R.id.btnDownvote);
            btnAcceptAnswer = itemView.findViewById(R.id.btnAcceptAnswer);
            acceptedBadge = itemView.findViewById(R.id.acceptedBadge);
        }
        
        public void bind(AnswerWithUser answerWithUser) {
            // Set user info
            answerUserName.setText(answerWithUser.userInfo.name);
            answerUserDept.setText(answerWithUser.userInfo.department + " • Year " + 
                    answerWithUser.userInfo.academicYear);
            
            // Set answer content
            answerContent.setText(answerWithUser.answer.getContent());
            
            // Set vote counts
            btnUpvote.setText("👍 " + answerWithUser.answer.getUpvotes());
            btnDownvote.setText("👎 " + answerWithUser.answer.getDownvotes());
            
            // Show accept button only if current user is question owner and answer not accepted
            boolean isQuestionOwner = currentUserId == questionOwnerId;
            boolean isAccepted = answerWithUser.answer.isAccepted();
            
            if (isQuestionOwner && !isAccepted) {
                btnAcceptAnswer.setVisibility(View.VISIBLE);
                acceptedBadge.setVisibility(View.GONE);
            } else if (isAccepted) {
                btnAcceptAnswer.setVisibility(View.GONE);
                acceptedBadge.setVisibility(View.VISIBLE);
            } else {
                btnAcceptAnswer.setVisibility(View.GONE);
                acceptedBadge.setVisibility(View.GONE);
            }
            
            // Set click listeners
            btnUpvote.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpvote(answerWithUser.answer.getAnswerId());
                }
            });
            
            btnDownvote.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownvote(answerWithUser.answer.getAnswerId());
                }
            });
            
            btnAcceptAnswer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptAnswer(answerWithUser.answer.getAnswerId());
                }
            });
        }
    }
}
