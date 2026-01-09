package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Notification entity mapping to 'notifications' table
 */
@Entity(
    tableName = "notifications",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id")}
)
public class Notification {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    private long notificationId;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "message")
    private String message;
    
    @ColumnInfo(name = "type")
    private String type; // ANSWER, VOTE, ACCEPTED, COIN, etc.
    
    @ColumnInfo(name = "reference_id")
    private Long referenceId; // question_id or answer_id
    
    @ColumnInfo(name = "is_read")
    private boolean isRead;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public Notification() {
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
