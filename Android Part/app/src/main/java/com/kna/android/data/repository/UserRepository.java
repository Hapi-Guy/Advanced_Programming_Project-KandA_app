package com.kna.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.kna.android.data.dao.UserDao;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.User;
import com.kna.android.util.PasswordHasher;
import com.kna.android.util.SessionManager;
import com.kna.android.util.ValidationUtils;

import java.util.List;
import java.util.concurrent.Future;

/**
 * User Repository - migrated from desktop AuthService.java
 * Handles authentication and user management business logic
 */
public class UserRepository {
    
    private final UserDao userDao;
    private final KnADatabase database;
    
    public UserRepository(Application application) {
        database = KnADatabase.getDatabase(application);
        userDao = database.userDao();
    }
    
    /**
     * Register new user - from desktop AuthService.register()
     * Validation rules:
     * - Email must be valid format
     * - Email must be unique
     * - Password minimum 6 characters
     * - Academic year 1-5
     */
    public Future<RegisterResult> register(String email, String password, String name, 
                                           String department, int academicYear, String phoneNumber) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            // Validate email format
            if (!ValidationUtils.isValidEmail(email)) {
                return new RegisterResult(false, "Invalid email format");
            }
            
            // Validate password
            if (!ValidationUtils.isValidPassword(password)) {
                return new RegisterResult(false, "Password must be at least 6 characters");
            }
            
            // Validate academic year
            if (academicYear < 1 || academicYear > 5) {
                return new RegisterResult(false, "Academic year must be between 1 and 5");
            }
            
            // Check if email already exists
            if (userDao.emailExists(email) > 0) {
                return new RegisterResult(false, "Email already registered");
            }
            
            // Create new user
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(password); // Plain text password (simplified)
            user.setName(name);
            user.setDepartment(department);
            user.setAcademicYear(academicYear);
            user.setPhoneNumber(phoneNumber);
            user.setCoins(100); // Initial coins as per desktop app
            user.setCreatedAt(System.currentTimeMillis());
            user.setLastLogin(System.currentTimeMillis());
            
            long userId = userDao.insert(user);
            user.setUserId(userId);
            
            return new RegisterResult(true, "Registration successful", user);
        });
    }
    
    /**
     * Login user - from desktop AuthService.login()
     */
    public Future<LoginResult> login(String email, String password, boolean isAdmin) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            // Trim inputs to avoid whitespace issues
            String trimmedEmail = (email != null) ? email.trim().toLowerCase() : "";
            String trimmedPassword = (password != null) ? password.trim() : "";
            
            android.util.Log.d("UserRepository", "Login attempt - Email: " + trimmedEmail + ", Password length: " + trimmedPassword.length());
            
            // Validate inputs
            if (trimmedEmail.isEmpty()) {
                return new LoginResult(false, "Email is required");
            }
            if (trimmedPassword.isEmpty()) {
                return new LoginResult(false, "Password is required");
            }
            
            // Get user by email
            User user = userDao.getUserByEmail(trimmedEmail);
            
            android.util.Log.d("UserRepository", "User found: " + (user != null));
            
            if (user == null) {
                android.util.Log.d("UserRepository", "No user found with email: " + trimmedEmail);
                return new LoginResult(false, "Invalid email or password");
            }
            
            android.util.Log.d("UserRepository", "Stored password: " + user.getPasswordHash() + ", Input password: " + trimmedPassword);
            
            // Verify password (plain text string comparison)
            if (!trimmedPassword.equals(user.getPasswordHash())) {
                android.util.Log.d("UserRepository", "Password mismatch!");
                return new LoginResult(false, "Invalid email or password");
            }
            
            // Check admin role
            if (isAdmin && !user.isAdmin()) {
                return new LoginResult(false, "You do not have admin privileges");
            }
            
            // Update last login
            userDao.updateLastLogin(user.getUserId(), System.currentTimeMillis());
            
            return new LoginResult(true, "Login successful", user);
        });
    }
    
    /**
     * Get user by ID
     */
    public LiveData<User> getUserById(long userId) {
        return userDao.getUserById(userId);
    }
    
    /**
     * Get top users by reputation (for leaderboard)
     */
    public LiveData<List<User>> getTopUsersByReputation(int limit) {
        return userDao.getTopUsersByReputation(limit);
    }
    
    public LiveData<List<User>> getAllUsersByReputation() {
        return userDao.getAllUsersByReputation();
    }
    
    /**
     * Update user profile
     */
    public void updateUser(User user) {
        KnADatabase.databaseWriteExecutor.execute(() -> {
            userDao.update(user);
        });
    }
    
    /**
     * Change password
     */
    public Future<Boolean> changePassword(long userId, String oldPassword, String newPassword) {
        return KnADatabase.databaseWriteExecutor.submit(() -> {
            User user = userDao.getUserByIdSync(userId);
            
            if (user == null) {
                return false;
            }
            
            // Verify old password (plain text)
            if (!oldPassword.equals(user.getPasswordHash())) {
                return false;
            }
            
            // Validate new password
            if (!ValidationUtils.isValidPassword(newPassword)) {
                return false;
            }
            
            // Update password (plain text)
            userDao.updatePassword(userId, newPassword);
            
            return true;
        });
    }
    
    /**
     * Reset password (admin function)
     */
    public void resetPassword(long userId, String newPassword) {
        KnADatabase.databaseWriteExecutor.execute(() -> {
            userDao.updatePassword(userId, newPassword); // Plain text
        });
    }
    
    // Result classes
    public static class RegisterResult {
        public final boolean success;
        public final String message;
        public final User user;
        
        public RegisterResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public RegisterResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }
    
    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final User user;
        
        public LoginResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }
}
