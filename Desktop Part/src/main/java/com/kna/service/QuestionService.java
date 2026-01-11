package com.kna.service;

import com.kna.dao.QuestionDAO;
import com.kna.dao.UserDAO;
import com.kna.dao.CoinDAO;
import com.kna.dao.NotificationDAO;
import com.kna.dao.AnswerDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.model.CoinTransaction;
import com.kna.util.SessionManager;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * QuestionService - Business logic for questions
 */
public class QuestionService {
    
    private final QuestionDAO questionDAO;
    private final UserDAO userDAO;
    private final CoinDAO coinDAO;
    private final NotificationDAO notificationDAO;
    private final AnswerDAO answerDAO;
    
    // Coin costs
    private static final int BASE_QUESTION_COST = 20;
    private static final int URGENT_QUESTION_COST = 30;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
        this.userDAO = new UserDAO();
        this.coinDAO = new CoinDAO();
        this.notificationDAO = new NotificationDAO();
        this.answerDAO = new AnswerDAO();
    }

    /**
     * Ask a new question
     */
    public Question askQuestion(String title, String description, String category, 
                                boolean isUrgent, File imageFile) throws Exception {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        // Check if user has unevaluated questions limit
        int unevaluatedCount = questionDAO.getUnevaluatedCount(currentUser.getUserId());
        if (unevaluatedCount >= 5) {
            throw new Exception("You have reached the limit of unevaluated questions (5). " +
                              "Please evaluate your answered questions before asking new ones.");
        }
        
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            throw new Exception("Title is required");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new Exception("Description is required");
        }
        
        if (category == null || category.trim().isEmpty()) {
            throw new Exception("Category is required");
        }
        
        // Calculate coin cost
        int coinCost = isUrgent ? URGENT_QUESTION_COST : BASE_QUESTION_COST;
        int coinReward = coinCost; // Reward is same as cost
        
        // Check if user has enough coins
        if (currentUser.getCoins() < coinCost) {
            throw new Exception("Insufficient coins. You need " + coinCost + " Coins to ask this question.");
        }
        
        // Create question
        Question question = new Question(
            currentUser.getUserId(),
            title.trim(),
            description.trim(),
            category,
            isUrgent,
            coinReward
        );
        
        try {
            // Save question
            int questionId = questionDAO.createQuestion(question);
            question.setQuestionId(questionId);
            
            // Save image if provided
            if (imageFile != null) {
                String imagePath = com.kna.util.ImageLoader.saveQuestionImage(
                    imageFile, currentUser.getUserId(), questionId
                );
                if (imagePath != null) {
                    questionDAO.addQuestionImage(questionId, imagePath);
                    question.setImagePath(imagePath);
                }
            }
            
            // Deduct coins from user
            int newBalance = currentUser.getCoins() - coinCost;
            userDAO.updateCoins(currentUser.getUserId(), newBalance);
            
            // Record transaction
            CoinTransaction transaction = new CoinTransaction(
                currentUser.getUserId(),
                -coinCost,
                "spent",
                "Asked question: " + title,
                newBalance
            );
            transaction.setReferenceId(questionId);
            transaction.setReferenceType("question");
            coinDAO.createTransaction(transaction);
            
            // Update user stats
            userDAO.incrementQuestions(currentUser.getUserId());
            
            // Update session
            SessionManager.getInstance().updateCoins(newBalance);
            currentUser.setCoins(newBalance);
            
            // Check for low balance
            if (newBalance < BASE_QUESTION_COST) {
                notificationDAO.notifyLowBalance(currentUser.getUserId(), newBalance);
            }
            
            return question;
            
        } catch (SQLException e) {
            throw new Exception("Failed to ask question: " + e.getMessage());
        }
    }

    /**
     * Get question by ID
     */
    public Question getQuestion(int questionId) throws SQLException {
        Question question = questionDAO.findById(questionId);
        if (question != null) {
            // Increment view count
            questionDAO.incrementViewCount(questionId);
            
            // Load image path if exists
            String imagePath = questionDAO.getQuestionImagePath(questionId);
            question.setImagePath(imagePath);
        }
        return question;
    }

    /**
     * Get questions with filters
     */
    public List<Question> getQuestions(String category, Boolean isUrgent, Boolean isUnanswered, 
                                       int limit, int offset) throws SQLException {
        return questionDAO.getAllQuestions(category, isUrgent, isUnanswered, limit, offset);
    }

    /**
     * Get user's questions
     */
    public List<Question> getUserQuestions(int userId) throws SQLException {
        return questionDAO.getQuestionsByUserId(userId);
    }

    /**
     * Search questions
     */
    public List<Question> searchQuestions(String searchTerm) throws SQLException {
        return questionDAO.searchQuestions(searchTerm);
    }

    /**
     * Delete question (admin only or question owner)
     * Also deletes all answers associated with the question (cascade delete)
     */
    public boolean deleteQuestion(int questionId) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        Question question = questionDAO.findById(questionId);
        if (question == null) {
            throw new Exception("Question not found");
        }
        
        // Check permissions
        if (!currentUser.isAdmin() && question.getUserId() != currentUser.getUserId()) {
            throw new Exception("You don't have permission to delete this question");
        }
        
        try {
            // First, delete all answers associated with this question (cascade delete)
            answerDAO.deleteAnswersByQuestionId(questionId);
            
            // Then delete the question
            return questionDAO.deleteQuestion(questionId);
        } catch (SQLException e) {
            throw new Exception("Failed to delete question: " + e.getMessage());
        }
    }

    /**
     * Check if user can ask questions
     */
    public boolean canAskQuestion(int userId) throws SQLException {
        int unevaluatedCount = questionDAO.getUnevaluatedCount(userId);
        return unevaluatedCount < 5;
    }

    /**
     * Get unevaluated questions count
     */
    public int getUnevaluatedCount(int userId) throws SQLException {
        return questionDAO.getUnevaluatedCount(userId);
    }

    /**
     * Get coin cost for question
     */
    public int getCoinCost(boolean isUrgent) {
        return isUrgent ? URGENT_QUESTION_COST : BASE_QUESTION_COST;
    }
}
