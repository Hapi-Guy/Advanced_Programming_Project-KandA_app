package com.kna.dao;

import com.kna.model.User;
import com.kna.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object for User entity
 */
public class UserDAO {
    
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Create a new user
     */
    public int createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (email, phone, password_hash, name, department, academic_year, coins) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            user.getEmail(),
            user.getPhone(),
            user.getPasswordHash(),
            user.getName(),
            user.getDepartment(),
            user.getAcademicYear(),
            user.getCoins() > 0 ? user.getCoins() : 100
        );
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        
        return null;
    }

    /**
     * Find user by ID
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        
        return null;
    }

    /**
     * Update user profile
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, phone = ?, department = ?, academic_year = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        int rowsAffected = dbManager.executeUpdate(sql,
            user.getName(),
            user.getPhone(),
            user.getDepartment(),
            user.getAcademicYear(),
            user.getUserId()
        );
        
        return rowsAffected > 0;
    }

    /**
     * Update user coins
     */
    public boolean updateCoins(int userId, int newBalance) throws SQLException {
        String sql = "UPDATE users SET coins = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        int rowsAffected = dbManager.executeUpdate(sql, newBalance, userId);
        return rowsAffected > 0;
    }

    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, phone = ?, department = ?, academic_year = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try {
            int rowsAffected = dbManager.executeUpdate(sql, 
                user.getName(), 
                user.getPhone(), 
                user.getDepartment(), 
                user.getAcademicYear(), 
                user.getId()
            );
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update user reputation
     */
    public boolean updateReputation(int userId, int reputation) throws SQLException {
        String sql = "UPDATE users SET reputation = reputation + ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        int rowsAffected = dbManager.executeUpdate(sql, reputation, userId);
        return rowsAffected > 0;
    }

    /**
     * Increment total questions count
     */
    public boolean incrementQuestions(int userId) throws SQLException {
        String sql = "UPDATE users SET total_questions = total_questions + 1 WHERE user_id = ?";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Increment total answers count
     */
    public boolean incrementAnswers(int userId) throws SQLException {
        String sql = "UPDATE users SET total_answers = total_answers + 1 WHERE user_id = ?";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Increment accepted answers count
     */
    public boolean incrementAcceptedAnswers(int userId) throws SQLException {
        String sql = "UPDATE users SET accepted_answers = accepted_answers + 1 WHERE user_id = ?";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Get all users (for admin)
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        
        return users;
    }

    /**
     * Search users by name or email
     */
    public List<User> searchUsers(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM users WHERE (name LIKE ? OR email LIKE ?) AND is_active = 1 LIMIT 20";
        List<User> users = new ArrayList<>();
        
        String searchPattern = "%" + searchTerm + "%";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        
        return users;
    }
    
    /**
     * Get top users by reputation (for leaderboard)
     */
    public List<User> getTopUsersByReputation(int limit) throws SQLException {
        String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY reputation DESC, total_answers DESC LIMIT ?";
        List<User> users = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        
        return users;
    }

    /**
     * Deactivate user account
     */
    public boolean deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }

    /**
     * Extract User object from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setName(rs.getString("name"));
        user.setDepartment(rs.getString("department"));
        user.setAcademicYear(rs.getInt("academic_year"));
        user.setCoins(rs.getInt("coins"));
        user.setReputation(rs.getInt("reputation"));
        user.setTotalQuestions(rs.getInt("total_questions"));
        user.setTotalAnswers(rs.getInt("total_answers"));
        user.setAcceptedAnswers(rs.getInt("accepted_answers"));
        user.setActive(rs.getBoolean("is_active"));
        user.setAdmin(rs.getBoolean("is_admin"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
