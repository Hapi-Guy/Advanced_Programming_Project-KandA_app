package com.kna.model;

import java.sql.Timestamp;

/**
 * Notification model representing system notifications
 */
public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String notificationType; // answer, accepted, earned, low_balance, announcement, warning
    private boolean isRead;
    private Integer referenceId;
    private String referenceType;
    private Timestamp createdAt;

    // Constructors
    public Notification() {}

    public Notification(int userId, String title, String message, String notificationType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Alias for getNotificationId
    public int getId() {
        return notificationId;
    }

    public void setId(int id) {
        this.notificationId = id;
    }

    // Alias for getNotificationType
    public String getType() {
        return notificationType;
    }

    public void setType(String type) {
        this.notificationType = type;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", title='" + title + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
