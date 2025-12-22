package com.kna.model;

import java.sql.Timestamp;

/**
 * Session model for maintaining user login sessions
 */
public class Session {
    private int sessionId;
    private int userId;
    private String sessionToken;
    private Timestamp createdAt;
    private Timestamp expiresAt;

    // Constructors
    public Session() {}

    public Session(int userId, String sessionToken, Timestamp expiresAt) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }
}
