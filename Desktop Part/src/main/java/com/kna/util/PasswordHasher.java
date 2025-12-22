package com.kna.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHasher utility for secure password hashing using BCrypt
 */
public class PasswordHasher {
    
    // BCrypt work factor (number of rounds)
    private static final int WORK_FACTOR = 10;

    /**
     * Hash a plaintext password using BCrypt
     * @param plainPassword The plaintext password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verify a plaintext password against a hashed password
     * @param plainPassword The plaintext password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate password strength
     * @param password The password to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Password must contain at least:
        // - 6 characters minimum
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }

    /**
     * Get password strength message
     * @param password The password to check
     * @return Message describing password requirements
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        if (!password.matches(".*[a-zA-Z].*")) {
            return "Password must contain at least one letter";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        
        return "Password is valid";
    }
}
