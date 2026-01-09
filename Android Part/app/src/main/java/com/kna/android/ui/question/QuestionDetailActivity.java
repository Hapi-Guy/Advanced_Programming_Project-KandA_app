package com.kna.android.ui.question;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kna.android.R;
import com.kna.android.ui.adapter.AnswerAdapter;
import com.kna.android.ui.viewmodel.QuestionDetailViewModel;
import com.kna.android.util.Constants;
import com.kna.android.util.SessionManager;

/**
 * Question Detail Activity - migrated from desktop QuestionDetailController
 * Shows full question with answers
 */
public class QuestionDetailActivity extends AppCompatActivity {
    
    private QuestionDetailViewModel viewModel;
    private AnswerAdapter answerAdapter;
    
    private TextView userNameText;
    private TextView userDeptText;
    private TextView questionTitle;
    private TextView questionDescription;
    private TextView categoryBadge;
    private TextView urgentBadge;
    private TextView coinRewardText;
    private TextView answersHeader;
    private RecyclerView answersRecyclerView;
    private TextInputEditText answerInput;
    private MaterialButton btnSubmitAnswer;
    
    private long questionId;
    private long currentUserId;
    private long questionOwnerId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);
        
        // Get current user
        SessionManager sessionManager = SessionManager.getInstance(this);
        currentUserId = sessionManager.getCurrentUserId();
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Initialize views
        userNameText = findViewById(R.id.userNameText);
        userDeptText = findViewById(R.id.userDeptText);
        questionTitle = findViewById(R.id.questionTitle);
        questionDescription = findViewById(R.id.questionDescription);
        categoryBadge = findViewById(R.id.categoryBadge);
        urgentBadge = findViewById(R.id.urgentBadge);
        coinRewardText = findViewById(R.id.coinRewardText);
        answersHeader = findViewById(R.id.answersHeader);
        answersRecyclerView = findViewById(R.id.answersRecyclerView);
        answerInput = findViewById(R.id.answerInput);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(QuestionDetailViewModel.class);
        
        // Get question ID from intent
        questionId = getIntent().getLongExtra(Constants.EXTRA_QUESTION_ID, -1);
        if (questionId == -1) {
            Toast.makeText(this, "Invalid question", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        viewModel.setQuestionId(questionId);
        
        // Setup RecyclerView
        answersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        answerAdapter = new AnswerAdapter(currentUserId, 0, new AnswerAdapter.OnAnswerActionListener() {
            @Override
            public void onUpvote(long answerId) {
                viewModel.voteAnswer(answerId, true);
            }
            
            @Override
            public void onDownvote(long answerId) {
                viewModel.voteAnswer(answerId, false);
            }
            
            @Override
            public void onAcceptAnswer(long answerId) {
                viewModel.acceptAnswer(answerId);
            }
        });
        answersRecyclerView.setAdapter(answerAdapter);
        
        // Submit answer button
        btnSubmitAnswer.setOnClickListener(v -> submitAnswer());
        
        // Setup observers
        setupObservers();
    }
    
    private void setupObservers() {
        // Remove any existing observers first
        viewModel.getQuestion().removeObservers(this);
        viewModel.getAnswers().removeObservers(this);
        viewModel.getSubmitResult().removeObservers(this);
        viewModel.getOperationResult().removeObservers(this);
        
        // Observe question
        viewModel.getQuestion().observe(this, questionWithUser -> {
            if (questionWithUser != null) {
                questionOwnerId = questionWithUser.question.getUserId();
                
                userNameText.setText(questionWithUser.userInfo.name);
                userDeptText.setText(questionWithUser.userInfo.department + " • Year " + 
                        questionWithUser.userInfo.academicYear);
                
                questionTitle.setText(questionWithUser.question.getTitle());
                questionDescription.setText(questionWithUser.question.getDescription());
                categoryBadge.setText(questionWithUser.question.getCategory());
                coinRewardText.setText("🪙 " + questionWithUser.question.getCoinReward() + " coins reward");
                
                if (questionWithUser.question.isUrgent()) {
                    urgentBadge.setVisibility(View.VISIBLE);
                } else {
                    urgentBadge.setVisibility(View.GONE);
                }
                
                // Update adapter with question owner ID
                answerAdapter = new AnswerAdapter(currentUserId, questionOwnerId, 
                        new AnswerAdapter.OnAnswerActionListener() {
                    @Override
                    public void onUpvote(long answerId) {
                        viewModel.voteAnswer(answerId, true);
                    }
                    
                    @Override
                    public void onDownvote(long answerId) {
                        viewModel.voteAnswer(answerId, false);
                    }
                    
                    @Override
                    public void onAcceptAnswer(long answerId) {
                        viewModel.acceptAnswer(answerId);
                    }
                });
                answersRecyclerView.setAdapter(answerAdapter);
            }
        });
        
        // Observe answers
        viewModel.getAnswers().observe(this, answers -> {
            if (answers != null) {
                answerAdapter.setAnswers(answers);
                answersHeader.setText(answers.size() + " Answer" + (answers.size() != 1 ? "s" : ""));
            }
        });
        
        // Observe submit result
        viewModel.getSubmitResult().observe(this, result -> {
            if (result != null) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                if (result.success) {
                    answerInput.setText("");
                    // Force reload answers after successful submission (delayed to avoid observer conflicts)
                    answerInput.postDelayed(() -> {
                        viewModel.reloadAnswers();
                        reObserveAnswers();
                    }, 100);
                }
            }
        });
        
        // Observe operation result (for votes and accept)
        viewModel.getOperationResult().observe(this, result -> {
            if (result != null) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                if (result.success) {
                    // Force reload answers after vote or accept (delayed to avoid observer conflicts)
                    answerInput.postDelayed(() -> {
                        viewModel.reloadAnswers();
                        reObserveAnswers();
                    }, 100);
                }
            }
        });
    }
    
    private void reObserveAnswers() {
        // Remove only the answers observer and re-add it
        viewModel.getAnswers().removeObservers(this);
        viewModel.getAnswers().observe(this, answers -> {
            if (answers != null) {
                answerAdapter.setAnswers(answers);
                answersHeader.setText(answers.size() + " Answer" + (answers.size() != 1 ? "s" : ""));
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload answers when returning to this activity
        if (viewModel != null && questionId != -1) {
            viewModel.reloadAnswers();
            reObserveAnswers();
        }
    }
    
    private void submitAnswer() {
        String content = answerInput.getText() != null ? answerInput.getText().toString().trim() : "";
        
        if (content.isEmpty()) {
            answerInput.setError("Answer cannot be empty");
            return;
        }
        
        viewModel.submitAnswer(content);
    }
}
