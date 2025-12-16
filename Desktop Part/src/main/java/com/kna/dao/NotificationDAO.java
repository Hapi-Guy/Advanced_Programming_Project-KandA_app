package com.kna.dao;

import com.kna.model.Notification;
import com.kna.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO - Data Access Object for Notification entity
 */
public class NotificationDAO {
    
    private final DatabaseManager dbManager;

    public NotificationDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Create a new notification
     */
    public int createNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, " +
                     "reference_id, reference_type) VALUES (?, ?, ?, ?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            notification.getUserId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getNotificationType(),
            notification.getReferenceId(),
            notification.getReferenceType()
        );
    }

    /**
     * Get notifications by user ID
     */
    public List<Notification> getNotificationsByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        
        List<Notification> notifications = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        }
        
        return notifications;
    }
    
    /**
     * Get all notifications by user ID (no limit)
     */
    public List<Notification> getNotificationsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        
        List<Notification> notifications = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        }
        
        return notifications;
    }
    
    /**
     * Get unread notifications by user ID
     */
    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";
        
        List<Notification> notifications = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        }
        
        return notifications;
    }
    
    /**
     * Get notifications by type
     */
    public List<Notification> getNotificationsByType(int userId, String type) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND notification_type = ? ORDER BY created_at DESC";
        
        List<Notification> notifications = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        }
        
        return notifications;
    }

    /**
     * Get unread notifications count
     */
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        
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
     * Mark notification as read
     */
    public boolean markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        return dbManager.executeUpdate(sql, notificationId) > 0;
    }

    /**
     * Mark all notifications as read for a user
     */
    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Delete notification
     */
    public boolean deleteNotification(int notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        return dbManager.executeUpdate(sql, notificationId) > 0;
    }

    /**
     * Delete all notifications for a user
     */
    public boolean deleteAllForUser(int userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        return dbManager.executeUpdate(sql, userId) > 0;
    }

    /**
     * Create notification for new answer
     */
    public void notifyNewAnswer(int userId, int questionId, String answererName) throws SQLException {
        Notification notification = new Notification(
            userId,
            "New Answer",
            answererName + " answered your question",
            "answer"
        );
        notification.setReferenceId(questionId);
        notification.setReferenceType("question");
        createNotification(notification);
    }

    /**
     * Create notification for accepted answer
     */
    public void notifyAcceptedAnswer(int userId, int answerId, int coinsEarned) throws SQLException {
        Notification notification = new Notification(
            userId,
            "Answer Accepted!",
            "Your answer was accepted! You earned " + coinsEarned + " Coins",
            "accepted"
        );
        notification.setReferenceId(answerId);
        notification.setReferenceType("answer");
        createNotification(notification);
    }

    /**
     * Create notification for coins earned
     */
    public void notifyCoinsEarned(int userId, int amount, String reason) throws SQLException {
        Notification notification = new Notification(
            userId,
            "Coins Earned",
            "You earned " + amount + " Coins - " + reason,
            "earned"
        );
        createNotification(notification);
    }

    /**
     * Create notification for low balance
     */
    public void notifyLowBalance(int userId, int currentBalance) throws SQLException {
        Notification notification = new Notification(
            userId,
            "Low Coin Balance",
            "Your coin balance is low (" + currentBalance + " Coins). Consider purchasing more.",
            "low_balance"
        );
        createNotification(notification);
    }

    /**
     * Create announcement notification
     */
    public void createAnnouncement(String title, String message) throws SQLException {
        // Get all active users
        String sql = "SELECT user_id FROM users WHERE is_active = 1";
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                Notification notification = new Notification(userId, title, message, "announcement");
                createNotification(notification);
            }
        }
    }

    /**
     * Extract Notification from ResultSet
     */
    private Notification extractNotificationFromResultSet(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setNotificationType(rs.getString("notification_type"));
        notification.setRead(rs.getBoolean("is_read"));
        
        int refId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            notification.setReferenceId(refId);
        }
        
        notification.setReferenceType(rs.getString("reference_type"));
        notification.setCreatedAt(rs.getTimestamp("created_at"));
        return notification;
    }
}
