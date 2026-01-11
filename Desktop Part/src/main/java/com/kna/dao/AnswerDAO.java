package com.kna.dao;

import com.kna.model.Answer;
import com.kna.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AnswerDAO - Data Access Object for Answer entity
 */
public class AnswerDAO {
    
    private final DatabaseManager dbManager;

    public AnswerDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Create a new answer
     */
    public int createAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (question_id, user_id, content) VALUES (?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            answer.getQuestionId(),
            answer.getUserId(),
            answer.getContent()
        );
    }

    /**
     * Get answer by ID
     */
    public Answer findById(int answerId) throws SQLException {
        String sql = "SELECT a.*, u.name as user_name FROM answers a " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.answer_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractAnswerFromResultSet(rs);
            }
        }
        
        return null;
    }

    /**
     * Get all answers for a question
     */
    public List<Answer> getAnswersByQuestionId(int questionId) throws SQLException {
        String sql = "SELECT a.*, u.name as user_name FROM answers a " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.question_id = ? " +
                     "ORDER BY a.is_accepted DESC, (a.upvotes - a.downvotes) DESC, a.created_at ASC";
        
        List<Answer> answers = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                answers.add(extractAnswerFromResultSet(rs));
            }
        }
        
        return answers;
    }

    /**
     * Get answers by user ID
     */
    public List<Answer> getAnswersByUserId(int userId) throws SQLException {
        String sql = "SELECT a.*, u.name as user_name FROM answers a " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.user_id = ? ORDER BY a.created_at DESC";
        
        List<Answer> answers = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                answers.add(extractAnswerFromResultSet(rs));
            }
        }
        
        return answers;
    }

    /**
     * Accept an answer
     */
    public boolean acceptAnswer(int answerId) throws SQLException {
        String sql = "UPDATE answers SET is_accepted = 1, updated_at = CURRENT_TIMESTAMP WHERE answer_id = ?";
        return dbManager.executeUpdate(sql, answerId) > 0;
    }

    /**
     * Rate an answer
     */
    public boolean rateAnswer(int answerId, int rating) throws SQLException {
        String sql = "UPDATE answers SET rating = ?, updated_at = CURRENT_TIMESTAMP WHERE answer_id = ?";
        return dbManager.executeUpdate(sql, rating, answerId) > 0;
    }

    /**
     * Vote on an answer
     */
    public boolean voteAnswer(int answerId, int userId, String voteType) throws SQLException {
        // First, check if user has already voted
        String checkSql = "SELECT vote_type FROM answer_votes WHERE answer_id = ? AND user_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(checkSql)) {
            pstmt.setInt(1, answerId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String existingVote = rs.getString("vote_type");
                
                if (existingVote.equals(voteType)) {
                    // Remove vote
                    return removeVote(answerId, userId, voteType);
                } else {
                    // Change vote
                    return changeVote(answerId, userId, voteType);
                }
            } else {
                // Add new vote
                return addVote(answerId, userId, voteType);
            }
        }
    }

    /**
     * Add vote
     */
    private boolean addVote(int answerId, int userId, String voteType) throws SQLException {
        String insertSql = "INSERT INTO answer_votes (answer_id, user_id, vote_type) VALUES (?, ?, ?)";
        dbManager.executeUpdate(insertSql, answerId, userId, voteType);
        
        String updateSql = voteType.equals("upvote") ?
            "UPDATE answers SET upvotes = upvotes + 1 WHERE answer_id = ?" :
            "UPDATE answers SET downvotes = downvotes + 1 WHERE answer_id = ?";
        
        return dbManager.executeUpdate(updateSql, answerId) > 0;
    }

    /**
     * Remove vote
     */
    private boolean removeVote(int answerId, int userId, String voteType) throws SQLException {
        String deleteSql = "DELETE FROM answer_votes WHERE answer_id = ? AND user_id = ?";
        dbManager.executeUpdate(deleteSql, answerId, userId);
        
        String updateSql = voteType.equals("upvote") ?
            "UPDATE answers SET upvotes = upvotes - 1 WHERE answer_id = ? AND upvotes > 0" :
            "UPDATE answers SET downvotes = downvotes - 1 WHERE answer_id = ? AND downvotes > 0";
        
        return dbManager.executeUpdate(updateSql, answerId) > 0;
    }

    /**
     * Change vote
     */
    private boolean changeVote(int answerId, int userId, String newVoteType) throws SQLException {
        String updateVoteSql = "UPDATE answer_votes SET vote_type = ? WHERE answer_id = ? AND user_id = ?";
        dbManager.executeUpdate(updateVoteSql, newVoteType, answerId, userId);
        
        // Increment new vote type and decrement old vote type
        String updateAnswerSql = newVoteType.equals("upvote") ?
            "UPDATE answers SET upvotes = upvotes + 1, downvotes = downvotes - 1 WHERE answer_id = ?" :
            "UPDATE answers SET downvotes = downvotes + 1, upvotes = upvotes - 1 WHERE answer_id = ?";
        
        return dbManager.executeUpdate(updateAnswerSql, answerId) > 0;
    }

    /**
     * Get user's vote on an answer
     */
    public String getUserVote(int answerId, int userId) throws SQLException {
        String sql = "SELECT vote_type FROM answer_votes WHERE answer_id = ? AND user_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("vote_type");
            }
        }
        
        return null;
    }

    /**
     * Delete answer
     */
    public boolean deleteAnswer(int answerId) throws SQLException {
        String sql = "DELETE FROM answers WHERE answer_id = ?";
        return dbManager.executeUpdate(sql, answerId) > 0;
    }

    /**
     * Delete all answers for a specific question (cascade delete).
     * @param questionId The question ID
     * @return Number of answers deleted
     */
    public int deleteAnswersByQuestionId(int questionId) throws SQLException {
        String sql = "DELETE FROM answers WHERE question_id = ?";
        return dbManager.executeUpdate(sql, questionId);
    }

    /**
     * Extract Answer object from ResultSet
     */
    private Answer extractAnswerFromResultSet(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setAnswerId(rs.getInt("answer_id"));
        answer.setQuestionId(rs.getInt("question_id"));
        answer.setUserId(rs.getInt("user_id"));
        answer.setUserName(rs.getString("user_name"));
        answer.setContent(rs.getString("content"));
        answer.setAccepted(rs.getBoolean("is_accepted"));
        answer.setRating(rs.getInt("rating"));
        answer.setUpvotes(rs.getInt("upvotes"));
        answer.setDownvotes(rs.getInt("downvotes"));
        answer.setCreatedAt(rs.getTimestamp("created_at"));
        answer.setUpdatedAt(rs.getTimestamp("updated_at"));
        return answer;
    }
}
