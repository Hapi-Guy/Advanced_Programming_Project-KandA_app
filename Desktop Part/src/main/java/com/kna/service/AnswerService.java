package com.kna.service;

import com.kna.dao.AnswerDAO;
import com.kna.dao.QuestionDAO;
import com.kna.dao.UserDAO;
import com.kna.dao.CoinDAO;
import com.kna.dao.NotificationDAO;
import com.kna.model.Answer;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.model.CoinTransaction;
import com.kna.util.SessionManager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * AnswerService - Business logic for answers
 */
public class AnswerService {
    
    private final AnswerDAO answerDAO;
    private final QuestionDAO questionDAO;
    private final UserDAO userDAO;
    private final CoinDAO coinDAO;
    private final NotificationDAO notificationDAO;
    
    private static final int REPUTATION_PER_UPVOTE = 10;
    private static final int REPUTATION_PER_ACCEPTED = 50;
    private static final int URGENT_BONUS_MULTIPLIER = 2;

    public AnswerService() {
        this.answerDAO = new AnswerDAO();
        this.questionDAO = new QuestionDAO();
        this.userDAO = new UserDAO();
        this.coinDAO = new CoinDAO();
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Submit an answer
     */
    public Answer submitAnswer(int questionId, String content) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new Exception("Answer content is required");
        }
        
        // Get question
        Question question = questionDAO.findById(questionId);
        if (question == null) {
            throw new Exception("Question not found");
        }
        
        // Don't allow answering own question
        if (question.getUserId() == currentUser.getUserId()) {
            throw new Exception("You cannot answer your own question");
        }
        
        try {
            // Create answer
            Answer answer = new Answer(questionId, currentUser.getUserId(), content.trim());
            int answerId = answerDAO.createAnswer(answer);
            answer.setAnswerId(answerId);
            answer.setUserName(currentUser.getName());
            
            // Update user stats
            userDAO.incrementAnswers(currentUser.getUserId());
            
            // Notify question owner
            notificationDAO.notifyNewAnswer(question.getUserId(), questionId, currentUser.getName());
            
            return answer;
            
        } catch (SQLException e) {
            throw new Exception("Failed to submit answer: " + e.getMessage());
        }
    }

    /**
     * Accept an answer (question owner only)
     */
    public void acceptAnswer(int answerId, int rating) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        // Get answer
        Answer answer = answerDAO.findById(answerId);
        if (answer == null) {
            throw new Exception("Answer not found");
        }
        
        // Get question
        Question question = questionDAO.findById(answer.getQuestionId());
        if (question == null) {
            throw new Exception("Question not found");
        }
        
        // Check if current user is question owner
        if (question.getUserId() != currentUser.getUserId()) {
            throw new Exception("Only question owner can accept answers");
        }
        
        // Validate rating (0-5)
        if (rating < 0 || rating > 5) {
            throw new Exception("Rating must be between 0 and 5");
        }
        
        try {
            // Accept answer
            answerDAO.acceptAnswer(answerId);
            answerDAO.rateAnswer(answerId, rating);
            
            // Mark question as answered and evaluated
            questionDAO.markAsAnswered(question.getQuestionId(), answerId);
            
            // Calculate reward
            int coinsEarned = question.getCoinReward();
            int reputationEarned = REPUTATION_PER_ACCEPTED;
            
            // Bonus for fast answers on urgent questions
            if (question.isUrgent()) {
                Timestamp questionTime = question.getCreatedAt();
                Timestamp answerTime = answer.getCreatedAt();
                long timeDiff = answerTime.getTime() - questionTime.getTime();
                long minutesDiff = timeDiff / (1000 * 60);
                
                if (minutesDiff < 30) { // Answered within 30 minutes
                    coinsEarned *= URGENT_BONUS_MULTIPLIER;
                    reputationEarned *= URGENT_BONUS_MULTIPLIER;
                }
            }
            
            // Poor rating penalty
            if (rating < 2) {
                coinsEarned = coinsEarned / 2; // Half coins
                reputationEarned = 0; // No reputation
            }
            
            // Update answerer's coins and reputation
            User answerer = userDAO.findById(answer.getUserId());
            if (answerer != null) {
                int newBalance = answerer.getCoins() + coinsEarned;
                userDAO.updateCoins(answer.getUserId(), newBalance);
                userDAO.updateReputation(answer.getUserId(), reputationEarned);
                userDAO.incrementAcceptedAnswers(answer.getUserId());
                
                // Record transaction
                CoinTransaction transaction = new CoinTransaction(
                    answer.getUserId(),
                    coinsEarned,
                    "earned",
                    "Answer accepted for question: " + question.getTitle(),
                    newBalance
                );
                transaction.setReferenceId(answerId);
                transaction.setReferenceType("answer");
                coinDAO.createTransaction(transaction);
                
                // Notify answerer
                notificationDAO.notifyAcceptedAnswer(answer.getUserId(), answerId, coinsEarned);
            }
            
        } catch (SQLException e) {
            throw new Exception("Failed to accept answer: " + e.getMessage());
        }
    }

    /**
     * Vote on an answer
     */
    public void voteAnswer(int answerId, String voteType) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        // Get answer
        Answer answer = answerDAO.findById(answerId);
        if (answer == null) {
            throw new Exception("Answer not found");
        }
        
        // Don't allow voting on own answer
        if (answer.getUserId() == currentUser.getUserId()) {
            throw new Exception("You cannot vote on your own answer");
        }
        
        try {
            answerDAO.voteAnswer(answerId, currentUser.getUserId(), voteType);
            
            // Update reputation for upvote
            if (voteType.equals("upvote")) {
                userDAO.updateReputation(answer.getUserId(), REPUTATION_PER_UPVOTE);
            }
            
        } catch (SQLException e) {
            throw new Exception("Failed to vote: " + e.getMessage());
        }
    }

    /**
     * Get answers for a question
     */
    public List<Answer> getAnswers(int questionId) throws SQLException {
        return answerDAO.getAnswersByQuestionId(questionId);
    }

    /**
     * Get user's answers
     */
    public List<Answer> getUserAnswers(int userId) throws SQLException {
        return answerDAO.getAnswersByUserId(userId);
    }

    /**
     * Get user's vote on an answer
     */
    public String getUserVote(int answerId, int userId) throws SQLException {
        return answerDAO.getUserVote(answerId, userId);
    }

    /**
     * Delete answer
     */
    public boolean deleteAnswer(int answerId) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        Answer answer = answerDAO.findById(answerId);
        if (answer == null) {
            throw new Exception("Answer not found");
        }
        
        // Check permissions
        if (!currentUser.isAdmin() && answer.getUserId() != currentUser.getUserId()) {
            throw new Exception("You don't have permission to delete this answer");
        }
        
        try {
            return answerDAO.deleteAnswer(answerId);
        } catch (SQLException e) {
            throw new Exception("Failed to delete answer: " + e.getMessage());
        }
    }
}
