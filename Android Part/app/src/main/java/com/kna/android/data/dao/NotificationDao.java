package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kna.android.data.model.Notification;

import java.util.List;

/**
 * Notification DAO - migrated from desktop NotificationDAO.java
 */
@Dao
public interface NotificationDao {
    
    @Insert
    long insert(Notification notification);
    
    @Update
    void update(Notification notification);
    
    @Delete
    void delete(Notification notification);
    
    /**
     * Get all notifications for user (unread first)
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY is_read ASC, created_at DESC")
    LiveData<List<Notification>> getNotificationsForUser(long userId);
    
    /**
     * Get unread notification count
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    LiveData<Integer> getUnreadCount(long userId);
    
    /**
     * Mark notification as read
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE notification_id = :notificationId")
    void markAsRead(long notificationId);
    
    /**
     * Mark all notifications as read for user
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    void markAllAsRead(long userId);
    
    /**
     * Delete all notifications for user
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId")
    void deleteAllForUser(long userId);
}
