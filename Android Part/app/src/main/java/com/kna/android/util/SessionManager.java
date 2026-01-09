package com.kna.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.kna.android.data.model.User;

/**
 * SessionManager - migrated from desktop SessionManager.java
 * Singleton pattern for managing current user session
 * Uses SharedPreferences for persistent login (unlike desktop's in-memory session)
 */
public class SessionManager {
    
    private static SessionManager instance;
    private final SharedPreferences prefs;
    private User currentUser;
    
    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    /**
     * Save login session
     */
    public void saveSession(User user) {
        this.currentUser = user;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.PREF_USER_ID, user.getUserId());
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_IS_ADMIN, user.isAdmin());
        editor.apply();
    }
    
    /**
     * Get current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Set current user (when loaded from database)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get current user ID from preferences
     */
    public long getCurrentUserId() {
        return prefs.getLong(Constants.PREF_USER_ID, -1);
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return prefs.getBoolean(Constants.PREF_IS_ADMIN, false);
    }
    
    /**
     * Clear session (logout)
     */
    public void clearSession() {
        this.currentUser = null;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * Update user coins in session
     */
    public void updateCoins(int coins) {
        if (currentUser != null) {
            currentUser.setCoins(coins);
        }
    }
    
    /**
     * Update user reputation in session
     */
    public void updateReputation(int reputation) {
        if (currentUser != null) {
            currentUser.setReputation(reputation);
        }
    }
}
