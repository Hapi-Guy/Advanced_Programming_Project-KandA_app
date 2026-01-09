package com.kna.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kna.android.data.model.Question;
import com.kna.android.data.repository.QuestionRepository;
import com.kna.android.util.SessionManager;

import java.util.concurrent.ExecutionException;

/**
 * AskQuestionViewModel - handles question creation
 * Migrated from desktop AskQuestionController
 */
public class AskQuestionViewModel extends AndroidViewModel {
    
    private final QuestionRepository questionRepository;
    private final SessionManager sessionManager;
    
    private final MutableLiveData<AskQuestionResult> askQuestionResult = new MutableLiveData<>();
    
    public AskQuestionViewModel(@NonNull Application application) {
        super(application);
        questionRepository = new QuestionRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }
    
    /**
     * Ask question with validation and coin deduction
     */
    public void askQuestion(String title, String description, String category, boolean isUrgent) {
        long userId = sessionManager.getCurrentUserId();
        
        new Thread(() -> {
            try {
                QuestionRepository.AskQuestionResult result = questionRepository.askQuestion(
                    userId, title, description, category, isUrgent
                ).get();
                
                askQuestionResult.postValue(new AskQuestionResult(
                    result.success, result.message, result.question
                ));
            } catch (ExecutionException | InterruptedException e) {
                askQuestionResult.postValue(new AskQuestionResult(
                    false, "Failed to post question: " + e.getMessage(), null
                ));
            }
        }).start();
    }
    
    public LiveData<AskQuestionResult> getAskQuestionResult() {
        return askQuestionResult;
    }
    
    /**
     * Result class
     */
    public static class AskQuestionResult {
        public final boolean success;
        public final String message;
        public final Question question;
        
        public AskQuestionResult(boolean success, String message, Question question) {
            this.success = success;
            this.message = message;
            this.question = question;
        }
    }
}
