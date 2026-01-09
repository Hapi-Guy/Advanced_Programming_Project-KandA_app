package com.kna.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.kna.android.data.model.Answer;
import com.kna.android.data.model.AnswerWithUser;
import com.kna.android.data.model.Question;
import com.kna.android.data.model.QuestionWithUser;
import com.kna.android.data.repository.AnswerRepository;
import com.kna.android.data.repository.QuestionRepository;
import com.kna.android.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * QuestionDetailViewModel - handles single question view with answers
 * Migrated from desktop QuestionDetailController
 */
public class QuestionDetailViewModel extends AndroidViewModel {
    
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final SessionManager sessionManager;
    
    private final MutableLiveData<Long> questionIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> operationResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> submitResult = new MutableLiveData<>();
    
    private LiveData<List<AnswerWithUser>> answersLiveData;
    
    public QuestionDetailViewModel(@NonNull Application application) {
        super(application);
        questionRepository = new QuestionRepository(application);
        answerRepository = new AnswerRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }
    
    public void setQuestionId(long questionId) {
        questionIdLiveData.setValue(questionId);
        // Increment view count
        questionRepository.incrementViewCount(questionId);
        // Initialize answers LiveData
        answersLiveData = answerRepository.getAnswersForQuestion(questionId);
    }
    
    public LiveData<QuestionWithUser> getQuestion() {
        if (questionIdLiveData.getValue() != null) {
            return questionRepository.getQuestionWithUserById(questionIdLiveData.getValue());
        }
        return new MutableLiveData<>();
    }
    
    public LiveData<List<AnswerWithUser>> getAnswers() {
        if (answersLiveData == null && questionIdLiveData.getValue() != null) {
            answersLiveData = answerRepository.getAnswersForQuestion(questionIdLiveData.getValue());
        }
        return answersLiveData != null ? answersLiveData : new MutableLiveData<>();
    }
    
    /**
     * Submit answer
     */
    public void submitAnswer(String content) {
        long userId = sessionManager.getCurrentUserId();
        long questionId = questionIdLiveData.getValue() != null ? questionIdLiveData.getValue() : 0;
        
        new Thread(() -> {
            try {
                AnswerRepository.SubmitAnswerResult result = answerRepository.submitAnswer(
                    userId, questionId, content
                ).get();
                
                submitResult.postValue(new OperationResult(
                    result.success, result.message
                ));
            } catch (ExecutionException | InterruptedException e) {
                submitResult.postValue(new OperationResult(
                    false, "Failed to submit answer: " + e.getMessage()
                ));
            }
        }).start();
    }
    
    /**
     * Accept answer (question owner only) - simplified without rating
     */
    public void acceptAnswer(long answerId) {
        long questionId = questionIdLiveData.getValue() != null ? questionIdLiveData.getValue() : 0;
        
        new Thread(() -> {
            try {
                // Default rating of 5 for simplicity
                AnswerRepository.AcceptAnswerResult result = answerRepository.acceptAnswer(
                    questionId, answerId, 5
                ).get();
                
                operationResult.postValue(new OperationResult(
                    result.success, result.message
                ));
            } catch (ExecutionException | InterruptedException e) {
                operationResult.postValue(new OperationResult(
                    false, "Failed to accept answer: " + e.getMessage()
                ));
            }
        }).start();
    }
    
    /**
     * Vote on answer (upvote/downvote)
     */
    public void voteAnswer(long answerId, boolean isUpvote) {
        long userId = sessionManager.getCurrentUserId();
        
        new Thread(() -> {
            try {
                AnswerRepository.VoteResult result = answerRepository.voteAnswer(
                    userId, answerId, isUpvote
                ).get();
                
                operationResult.postValue(new OperationResult(
                    result.success, result.message
                ));
            } catch (ExecutionException | InterruptedException e) {
                operationResult.postValue(new OperationResult(
                    false, "Failed to vote: " + e.getMessage()
                ));
            }
        }).start();
    }
    
    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }
    
    public LiveData<OperationResult> getSubmitResult() {
        return submitResult;
    }
    
    /**
     * Force reload answers (trigger LiveData update)
     */
    public void reloadAnswers() {
        if (questionIdLiveData.getValue() != null) {
            // Recreate the LiveData to force a fresh query
            answersLiveData = answerRepository.getAnswersForQuestion(questionIdLiveData.getValue());
        }
    }
    
    /**
     * Operation result class
     */
    public static class OperationResult {
        public final boolean success;
        public final String message;
        
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
