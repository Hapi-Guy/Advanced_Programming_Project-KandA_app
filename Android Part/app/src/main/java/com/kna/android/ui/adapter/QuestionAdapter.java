package com.kna.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kna.android.R;
import com.kna.android.data.model.QuestionWithUser;
import com.kna.android.util.DateTimeUtils;

/**
 * RecyclerView Adapter for Quora-style question feed
 * Displays question cards with user info, badges, and metadata
 */
public class QuestionAdapter extends ListAdapter<QuestionWithUser, QuestionAdapter.QuestionViewHolder> {
    
    private final OnQuestionClickListener clickListener;
    
    public interface OnQuestionClickListener {
        void onQuestionClick(QuestionWithUser question);
    }
    
    public QuestionAdapter(OnQuestionClickListener listener) {
        super(DIFF_CALLBACK);
        this.clickListener = listener;
    }
    
    private static final DiffUtil.ItemCallback<QuestionWithUser> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<QuestionWithUser>() {
            @Override
            public boolean areItemsTheSame(@NonNull QuestionWithUser oldItem, @NonNull QuestionWithUser newItem) {
                return oldItem.question.getQuestionId() == newItem.question.getQuestionId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull QuestionWithUser oldItem, @NonNull QuestionWithUser newItem) {
                return oldItem.question.getTitle().equals(newItem.question.getTitle()) &&
                       oldItem.question.getAnswerCount() == newItem.question.getAnswerCount();
            }
        };
    
    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_card, parent, false);
        return new QuestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionWithUser questionWithUser = getItem(position);
        holder.bind(questionWithUser, clickListener);
    }
    
    /**
     * ViewHolder for question card
     */
    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView userDepartment;
        private final TextView timestamp;
        private final TextView questionTitle;
        private final TextView questionDescription;
        private final TextView categoryTag;
        private final TextView urgentBadge;
        private final TextView coinReward;
        private final TextView answerCount;
        
        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userDepartment = itemView.findViewById(R.id.userDepartment);
            timestamp = itemView.findViewById(R.id.timestamp);
            questionTitle = itemView.findViewById(R.id.questionTitle);
            questionDescription = itemView.findViewById(R.id.questionDescription);
            categoryTag = itemView.findViewById(R.id.categoryTag);
            urgentBadge = itemView.findViewById(R.id.urgentBadge);
            coinReward = itemView.findViewById(R.id.coinReward);
            answerCount = itemView.findViewById(R.id.answerCount);
        }
        
        public void bind(QuestionWithUser questionWithUser, OnQuestionClickListener listener) {
            // User info
            userName.setText(questionWithUser.userInfo.name);
            userDepartment.setText(questionWithUser.userInfo.department + " • Year " + questionWithUser.userInfo.academicYear);
            
            // Timestamp
            timestamp.setText(DateTimeUtils.getRelativeTime(questionWithUser.question.getCreatedAt()));
            
            // Question content
            questionTitle.setText(questionWithUser.question.getTitle());
            questionDescription.setText(questionWithUser.question.getDescription());
            
            // Category tag
            categoryTag.setText(questionWithUser.question.getCategory());
            
            // Urgent badge
            if (questionWithUser.question.isUrgent()) {
                urgentBadge.setVisibility(View.VISIBLE);
            } else {
                urgentBadge.setVisibility(View.GONE);
            }
            
            // Coin reward
            coinReward.setText(String.valueOf(questionWithUser.question.getCoinReward()));
            
            // Answer count
            int count = questionWithUser.question.getAnswerCount();
            answerCount.setText(count + (count == 1 ? " answer" : " answers"));
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestionClick(questionWithUser);
                }
            });
        }
    }
}
