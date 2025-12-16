package com.kna.dao;

import com.kna.model.Question;
import com.kna.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestionDAO - Data Access Object for Question entity
 */
public class QuestionDAO {
    
    private final DatabaseManager dbManager;

    public QuestionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Create a new question
     */
    public int createQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (user_id, title, description, category, is_urgent, coin_reward) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            question.getUserId(),
            question.getTitle(),
            question.getDescription(),
            question.getCategory(),
            question.isUrgent() ? 1 : 0,
            question.getCoinReward()
        );
    }

    /**
     * Get question by ID
     */
    public Question findById(int questionId) throws SQLException {
        String sql = "SELECT q.*, u.name as user_name FROM questions q " +
                     "JOIN users u ON q.user_id = u.user_id " +
                     "WHERE q.question_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractQuestionFromResultSet(rs);
            }
        }
        
        return null;
    }

    /**
     * Get all questions with filters
     */
    public List<Question> getAllQuestions(String category, Boolean isUrgent, Boolean isUnanswered, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT q.*, u.name as user_name FROM questions q " +
            "JOIN users u ON q.user_id = u.user_id WHERE 1=1 "
        );
        
        List<Object> params = new ArrayList<>();
        
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append("AND q.category = ? ");
            params.add(category);
        }
        
        if (isUrgent != null && isUrgent) {
            sql.append("AND q.is_urgent = 1 ");
        }
        
        if (isUnanswered != null && isUnanswered) {
            sql.append("AND q.is_answered = 0 ");
        }
        
        sql.append("ORDER BY q.is_urgent DESC, q.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        List<Question> questions = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }
        }
        
        return questions;
    }

    /**
     * Get questions by user ID
     */
    public List<Question> getQuestionsByUserId(int userId) throws SQLException {
        String sql = "SELECT q.*, u.name as user_name FROM questions q " +
                     "JOIN users u ON q.user_id = u.user_id " +
                     "WHERE q.user_id = ? ORDER BY q.created_at DESC";
        
        List<Question> questions = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }
        }
        
        return questions;
    }

    /**
     * Get unevaluated questions count by user
     */
    public int getUnevaluatedCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE user_id = ? AND is_evaluated = 0 AND is_answered = 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Mark question as answered
     */
    public boolean markAsAnswered(int questionId, int answerId) throws SQLException {
        String sql = "UPDATE questions SET is_answered = 1, accepted_answer_id = ?, is_evaluated = 1, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE question_id = ?";
        
        return dbManager.executeUpdate(sql, answerId, questionId) > 0;
    }

    /**
     * Mark question as evaluated
     */
    public boolean markAsEvaluated(int questionId) throws SQLException {
        String sql = "UPDATE questions SET is_evaluated = 1, updated_at = CURRENT_TIMESTAMP WHERE question_id = ?";
        return dbManager.executeUpdate(sql, questionId) > 0;
    }

    /**
     * Increment view count
     */
    public boolean incrementViewCount(int questionId) throws SQLException {
        String sql = "UPDATE questions SET view_count = view_count + 1 WHERE question_id = ?";
        return dbManager.executeUpdate(sql, questionId) > 0;
    }

    /**
     * Search questions
     */
    public List<Question> searchQuestions(String searchTerm) throws SQLException {
        String sql = "SELECT q.*, u.name as user_name FROM questions q " +
                     "JOIN users u ON q.user_id = u.user_id " +
                     "WHERE q.title LIKE ? OR q.description LIKE ? " +
                     "ORDER BY q.created_at DESC LIMIT 50";
        
        List<Question> questions = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }
        }
        
        return questions;
    }

    /**
     * Delete question
     */
    public boolean deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        return dbManager.executeUpdate(sql, questionId) > 0;
    }

    /**
     * Add image to question
     */
    public boolean addQuestionImage(int questionId, String imagePath) throws SQLException {
        String sql = "INSERT INTO question_images (question_id, image_path) VALUES (?, ?)";
        return dbManager.executeUpdate(sql, questionId, imagePath) > 0;
    }

    /**
     * Get question image path
     */
    public String getQuestionImagePath(int questionId) throws SQLException {
        String sql = "SELECT image_path FROM question_images WHERE question_id = ? LIMIT 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("image_path");
            }
        }
        
        return null;
    }

    /**
     * Extract Question object from ResultSet
     */
    private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setQuestionId(rs.getInt("question_id"));
        question.setUserId(rs.getInt("user_id"));
        question.setUserName(rs.getString("user_name"));
        question.setTitle(rs.getString("title"));
        question.setDescription(rs.getString("description"));
        question.setCategory(rs.getString("category"));
        question.setUrgent(rs.getBoolean("is_urgent"));
        question.setCoinReward(rs.getInt("coin_reward"));
        question.setAnswered(rs.getBoolean("is_answered"));
        question.setEvaluated(rs.getBoolean("is_evaluated"));
        
        int acceptedAnswerId = rs.getInt("accepted_answer_id");
        if (!rs.wasNull()) {
            question.setAcceptedAnswerId(acceptedAnswerId);
        }
        
        question.setViewCount(rs.getInt("view_count"));
        question.setCreatedAt(rs.getTimestamp("created_at"));
        question.setUpdatedAt(rs.getTimestamp("updated_at"));
        return question;
    }
}
