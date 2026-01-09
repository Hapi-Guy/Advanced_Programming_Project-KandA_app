package com.kna.service;

import java.sql.SQLException;
import java.util.UUID;

import com.kna.dao.UserDAO;
import com.kna.model.User;
import com.kna.util.PasswordHasher;
import com.kna.util.SessionManager;

/**
 * AuthService - Handles authentication and registration
 */
public class AuthService {
    
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new user
     */
    public User register(String email, String phone, String password, String name, 
                        String department, int academicYear) throws Exception {
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email is required");
        }
        
        if (!isValidEmail(email)) {
            throw new Exception("Invalid email format");
        }
        
        if (password == null || password.length() < 6) {
            throw new Exception("Password must be at least 6 characters");
        }
        
        if (!PasswordHasher.isValidPassword(password)) {
            throw new Exception(PasswordHasher.getPasswordStrengthMessage(password));
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Name is required");
        }
        
        if (department == null || department.trim().isEmpty()) {
            throw new Exception("Department is required");
        }
        
        if (academicYear < 1 || academicYear > 5) {
            throw new Exception("Academic year must be between 1 and 5");
        }
        
        // Check if email already exists
        if (userDAO.emailExists(email)) {
            throw new Exception("Email already registered");
        }
        
        // Store password as plain text for testing
        // For production, use: String passwordHash = PasswordHasher.hashPassword(password);
        String passwordHash = password;
        
        // Create user object
        User user = new User();
        user.setEmail(email.trim());
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setName(name.trim());
        user.setDepartment(department);
        user.setAcademicYear(academicYear);
        user.setCoins(100); // Starting coins
        user.setReputation(0);
        user.setActive(true);
        user.setAdmin(false);
        
        // Save to database
        int userId = userDAO.createUser(user);
        user.setUserId(userId);
        
        return user;
    }

    /**
     * Login user
     */
    public User login(String email, String password) throws Exception {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email is required");
        }
        
        if (password == null || password.isEmpty()) {
            throw new Exception("Password is required");
        }
        
        // Find user by email
        User user = userDAO.findByEmail(email.trim());
        
        if (user == null) {
            throw new Exception("Invalid email or password");
        }
        
        // simple string comparison for testing
        if (!password.equals(user.getPasswordHash())) {
            throw new Exception("Invalid email or password");
        }
        

        
        // Set session
        SessionManager.getInstance().setCurrentUser(user);
        String sessionToken = generateSessionToken();
        SessionManager.getInstance().setSessionToken(sessionToken);
        
        return user;
    }

    /**
     * Logout current user
     */
    public void logout() {
        SessionManager.getInstance().logout();
    }

    /**
     * Change password
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) throws Exception {
        // Validate new password length
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("New password must be at least 6 characters");
        }
        
        // Get user
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }
        
        // Verify current password - simple string comparison for testing
        // For production, use: PasswordHasher.verifyPassword(currentPassword, user.getPasswordHash())
        if (!currentPassword.equals(user.getPasswordHash())) {
            throw new Exception("Current password is incorrect");
        }
        
        // Store new password as plain text for testing
        // For production, use: String newPasswordHash = PasswordHasher.hashPassword(newPassword);
        user.setPasswordHash(newPassword);
        
        try {
            userDAO.updateUser(user);
            
            // Update session with the new password
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUserId() == userId) {
                currentUser.setPasswordHash(newPassword);
                SessionManager.getInstance().setCurrentUser(currentUser);
            }
            
            return true;
        } catch (SQLException e) {
            throw new Exception("Failed to update password: " + e.getMessage());
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Generate session token
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get current logged in user
     */
    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }

    /**
     * Refresh current user data
     */
    public void refreshCurrentUser() throws SQLException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            User updatedUser = userDAO.findById(currentUser.getUserId());
            SessionManager.getInstance().setCurrentUser(updatedUser);
        }
    }
}
