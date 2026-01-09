package com.kna.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.kna.android.data.model.QuestionWithUser;
import com.kna.android.data.repository.QuestionRepository;
import com.kna.android.util.SessionManager;

import java.util.List;

/**
 * HomeViewModel - manages question feed for Quora-style home screen
 * Migrated from desktop DashboardController
 */
public class HomeViewModel extends AndroidViewModel {
    
    private final QuestionRepository questionRepository;
    private final SessionManager sessionManager;
    
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("all");
    private final LiveData<List<QuestionWithUser>> questions;
    
    public HomeViewModel(@NonNull Application application) {
        super(application);
        questionRepository = new QuestionRepository(application);
        sessionManager = SessionManager.getInstance(application);
        
        // Switch LiveData based on filter
        questions = Transformations.switchMap(currentFilter, filter -> {
            switch (filter) {
                case "urgent":
                    return questionRepository.getUrgentQuestions();
                case "my_dept":
                    String dept = sessionManager.getCurrentUser() != null ? 
                        sessionManager.getCurrentUser().getDepartment() : "CSE";
                    return questionRepository.getQuestionsByDepartment(dept);
                default:
                    return questionRepository.getAllQuestions();
            }
        });
    }
    
    public LiveData<List<QuestionWithUser>> getQuestions() {
        return questions;
    }
    
    public void setFilter(String filter) {
        currentFilter.setValue(filter);
    }
    
    public String getCurrentFilter() {
        return currentFilter.getValue();
    }
    
    public void refreshQuestions() {
        // Trigger refresh by re-setting the filter
        currentFilter.setValue(currentFilter.getValue());
    }
}
