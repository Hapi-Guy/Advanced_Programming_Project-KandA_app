package com.kna.util;

import java.util.HashMap;
import java.util.Map;

import com.kna.model.User;

/**
 * SessionManager - Manages current user session
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private String sessionToken;
    private Map<String, Object> sessionAttributes;

    private SessionManager() {
        sessionAttributes = new HashMap<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set current logged in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get current logged in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get current user ID
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Set session token
     */
    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    /**
     * Get session token
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
        sessionToken = null;
        sessionAttributes.clear();
    }

    /**
     * Clear session (alias for logout)
     */
    public void clearSession() {
        logout();
    }

    /**
     * Set session attribute
     */
    public void setAttribute(String key, Object value) {
        sessionAttributes.put(key, value);
    }

    /**
     * Get session attribute
     */
    public Object getAttribute(String key) {
        return sessionAttributes.get(key);
    }

    /**
     * Remove session attribute
     */
    public void removeAttribute(String key) {
        sessionAttributes.remove(key);
    }

    /**
     * Update current user's coin balance
     */
    public void updateCoins(int newBalance) {
        if (currentUser != null) {
            currentUser.setCoins(newBalance);
        }
    }

    /**
     * Update current user's reputation
     */
    public void updateReputation(int newReputation) {
        if (currentUser != null) {
            currentUser.setReputation(newReputation);
        }
    }

    /**
     * Refresh current user data
     */
    public void refreshUser(User updatedUser) {
        if (currentUser != null && updatedUser != null && 
            currentUser.getUserId() == updatedUser.getUserId()) {
            this.currentUser = updatedUser;
        }
    }
}
