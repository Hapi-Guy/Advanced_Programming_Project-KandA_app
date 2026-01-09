package com.kna.android.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * PasswordHasher - migrated from desktop PasswordHasher.java
 * Uses BCrypt for secure password hashing
 */
public class PasswordHasher {
    
    private static final int BCRYPT_COST = 12; // Cost factor for BCrypt
    
    /**
     * Hash password using BCrypt
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }
    
    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
    
    /**
     * Validate password strength (desktop validation logic)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }
}
