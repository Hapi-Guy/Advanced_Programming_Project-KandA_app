package com.kna.android.util;

import java.util.regex.Pattern;

/**
 * ValidationUtils - migrated from desktop validation logic
 * Contains all validation methods used across the app
 */
public class ValidationUtils {
    
    // Email regex pattern from desktop app
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && emailPattern.matcher(email).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }
    
    /**
     * Validate academic year (1-5)
     */
    public static boolean isValidAcademicYear(int year) {
        return year >= Constants.MIN_ACADEMIC_YEAR && year <= Constants.MAX_ACADEMIC_YEAR;
    }
    
    /**
     * Validate phone number (basic check)
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.length() >= 10 && phone.matches("\\d+");
    }
    
    /**
     * Validate question title
     */
    public static boolean isValidQuestionTitle(String title) {
        return title != null && title.trim().length() >= 10 && title.trim().length() <= 200;
    }
    
    /**
     * Validate question description
     */
    public static boolean isValidQuestionDescription(String description) {
        return description != null && description.trim().length() >= 20;
    }
    
    /**
     * Validate answer content
     */
    public static boolean isValidAnswerContent(String content) {
        return content != null && content.trim().length() >= 10;
    }
    
    /**
     * Validate rating (1-5 stars)
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }
}
