package com.kna.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.kna.android.data.dao.CoinTransactionDao;
import com.kna.android.data.dao.NotificationDao;
import com.kna.android.data.dao.QuestionDao;
import com.kna.android.data.dao.UserDao;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.CoinTransaction;
import com.kna.android.data.model.Notification;
import com.kna.android.data.model.Question;
import com.kna.android.data.model.QuestionWithUser;
import com.kna.android.data.model.User;
import com.kna.android.util.Constants;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Question Repository - migrated from desktop QuestionService.java
 * Handles question creation, validation, and coin management
 */
public class QuestionRepository {
    
    private final QuestionDao questionDao;
    private final UserDao userDao;
    private final CoinTransactionDao coinTransactionDao;
    private final NotificationDao notificationDao;
    private final KnADatabase database;
    
    public QuestionRepository(Application application) {
        database = KnADatabase.getDatabase(application);
        questionDao = database.questionDao();
        userDao = database.userDao();
        coinTransactionDao = database.coinTransactionDao();
        notificationDao = database.notificationDao();
    }
    
    /**
     * Ask question - from desktop QuestionService.askQuestion()
     * Business rules:
     * - Base cost: 20 coins
     * - Urgent cost: 30 coins
     * - Max 5 unevaluated questions per user
     * - Deduct coins from user
     * - Create coin transaction record
     */
    public Future<AskQuestionResult> askQuestion(long userId, String title, String description,
                                                  String category, boolean isUrgent) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            // Validate inputs
            if (title == null || title.trim().isEmpty()) {
                return new AskQuestionResult(false, "Title is required");
            }
            if (description == null || description.trim().isEmpty()) {
                return new AskQuestionResult(false, "Description is required");
            }
            if (category == null || category.trim().isEmpty()) {
                return new AskQuestionResult(false, "Category is required");
            }
            
            // Check unevaluated question limit (max 5)
            int unevaluatedCount = questionDao.getUnevaluatedQuestionCount(userId);
            if (unevaluatedCount >= Constants.MAX_UNEVALUATED_QUESTIONS) {
                return new AskQuestionResult(false, 
                    "You have reached the maximum of " + Constants.MAX_UNEVALUATED_QUESTIONS + " unevaluated questions");
            }
            
            // Calculate coin cost
            int cost = isUrgent ? Constants.URGENT_QUESTION_COST : Constants.BASE_QUESTION_COST;
            
            // Check if user has enough coins
            User user = userDao.getUserByIdSync(userId);
            if (user == null) {
                return new AskQuestionResult(false, "User not found");
            }
            if (user.getCoins() < cost) {
                return new AskQuestionResult(false, 
                    "Insufficient coins. You need " + cost + " coins but have only " + user.getCoins());
            }
            
            // Create question
            Question question = new Question();
            question.setUserId(userId);
            question.setTitle(title);
            question.setDescription(description);
            question.setCategory(category);
            question.setUrgent(isUrgent);
            question.setCoinReward(cost);
            question.setCreatedAt(System.currentTimeMillis());
            question.setUpdatedAt(System.currentTimeMillis());
            
            long questionId = questionDao.insert(question);
            question.setQuestionId(questionId);
            
            // Deduct coins from user
            userDao.deductCoins(userId, cost);
            
            // Increment total questions
            userDao.incrementTotalQuestions(userId);
            
            // Record coin transaction
            CoinTransaction transaction = new CoinTransaction();
            transaction.setUserId(userId);
            transaction.setAmount(-cost);
            transaction.setTransactionType("SPEND");
            transaction.setDescription("Asked question: " + title);
            transaction.setBalanceAfter(user.getCoins() - cost);
            coinTransactionDao.insert(transaction);
            
            return new AskQuestionResult(true, "Question posted successfully", question);
        });
    }
    
    /**
     * Get all questions (for home feed)
     */
    public LiveData<List<QuestionWithUser>> getAllQuestions() {
        return questionDao.getAllQuestionsWithUser();
    }
    
    /**
     * Get urgent questions
     */
    public LiveData<List<QuestionWithUser>> getUrgentQuestions() {
        return questionDao.getUrgentQuestions();
    }
    
    /**
     * Get questions by category
     */
    public LiveData<List<QuestionWithUser>> getQuestionsByCategory(String category) {
        return questionDao.getQuestionsByCategory(category);
    }
    
    /**
     * Get questions by department (for "My Dept" filter)
     */
    public LiveData<List<QuestionWithUser>> getQuestionsByDepartment(String department) {
        return questionDao.getQuestionsByDepartment(department);
    }
    
    /**
     * Get questions by user
     */
    public LiveData<List<Question>> getQuestionsByUser(long userId) {
        return questionDao.getQuestionsByUser(userId);
    }
    
    /**
     * Get question by ID
     */
    public LiveData<Question> getQuestionById(long questionId) {
        return questionDao.getQuestionById(questionId);
    }
    
    /**
     * Get question with user info by ID
     */
    public LiveData<QuestionWithUser> getQuestionWithUserById(long questionId) {
        return questionDao.getQuestionWithUserById(questionId);
    }
    
    /**
     * Increment view count
     */
    public void incrementViewCount(long questionId) {
        KnADatabase.databaseWriteExecutor.execute(() -> {
            questionDao.incrementViewCount(questionId);
        });
    }
    
    /**
     * Search questions
     */
    public LiveData<List<QuestionWithUser>> searchQuestions(String query) {
        return questionDao.searchQuestions(query);
    }
    
    /**
     * Delete question (admin only)
     */
    public void deleteQuestion(Question question) {
        KnADatabase.databaseWriteExecutor.execute(() -> {
            questionDao.delete(question);
        });
    }
    
    // Result class
    public static class AskQuestionResult {
        public final boolean success;
        public final String message;
        public final Question question;
        
        public AskQuestionResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public AskQuestionResult(boolean success, String message, Question question) {
            this.success = success;
            this.message = message;
            this.question = question;
        }
    }
}
