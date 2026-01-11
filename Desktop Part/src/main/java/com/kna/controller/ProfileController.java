package com.kna.controller;

import com.kna.Main;
import com.kna.dao.UserDAO;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the user profile page.
 * Displays user information, statistics, and allows editing profile details and password.
 */
public class ProfileController {
    
    @FXML private Button backButton;
    
    // Profile Header
    @FXML private Label avatarLabel;
    @FXML private Label usernameDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Label coinBalanceLabel;
    @FXML private Label reputationLabel;
    @FXML private Label roleBadge;
    
    // Statistics
    @FXML private Label questionsAskedLabel;
    @FXML private Label answersGivenLabel;
    @FXML private Label acceptedAnswersLabel;
    @FXML private Label memberSinceLabel;
    
    // Editable Fields
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    
    // Password Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Status
    @FXML private Label statusLabel;
    
    private UserDAO userDAO;
    private AuthService authService;
    private User currentUser;
    
    /**
     * Initialize the profile controller.
     * Called automatically after FXML loading.
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        authService = new AuthService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            loadUserProfile();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load and display user profile information.
     */
    private void loadUserProfile() {
        // Refresh user data from database
        try {
            currentUser = userDAO.findById(currentUser.getId());
            if (currentUser == null) {
                showError("Failed to load user data.");
                return;
            }
        } catch (Exception e) {
            showError("Failed to load user data.");
            return;
        }
        
        // Update session
        SessionManager.getInstance().setCurrentUser(currentUser);
        
        // Display profile header
        String initials = getInitials(currentUser.getName());
        avatarLabel.setText(initials);
        usernameDisplayLabel.setText(currentUser.getName());
        emailDisplayLabel.setText(currentUser.getEmail());
        coinBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
        // Admin users don't have reputation points
        reputationLabel.setText(currentUser.isAdmin() ? "0" : String.valueOf(currentUser.getReputation()));
        
        // Set role badge
        if (roleBadge != null) {
            roleBadge.setText(currentUser.isAdmin() ? "ADMIN" : "USER");
            if (currentUser.isAdmin()) {
                roleBadge.setStyle("-fx-background-color: #f44336;");
            }
        }
        
        // Display statistics
        questionsAskedLabel.setText(String.valueOf(currentUser.getQuestionsAsked()));
        answersGivenLabel.setText(String.valueOf(currentUser.getAnswersGiven()));
        acceptedAnswersLabel.setText(String.valueOf(currentUser.getAcceptedAnswers()));
        
        // Member since
        if (memberSinceLabel != null && currentUser.getCreatedAt() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
            memberSinceLabel.setText(currentUser.getCreatedAt().toLocalDateTime().format(formatter));
        }
        
        // Populate editable fields
        usernameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
    }
    
    /**
     * Get initials from a full name for avatar display.
     */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }
    
    /**
     * Save profile changes to database.
     */
    @FXML
    private void updateProfile() {
        String name = usernameField.getText().trim();
        String email = emailField.getText().trim();
        
        // Validation
        if (name.isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }
        
        if (email.isEmpty()) {
            showError("Email cannot be empty.");
            return;
        }
        
        // Update user object
        currentUser.setName(name);
        currentUser.setEmail(email);
        
        // Save to database
        try {
            userDAO.updateUser(currentUser);
            SessionManager.getInstance().setCurrentUser(currentUser);
            showSuccess("Profile updated successfully!");
            loadUserProfile(); // Refresh display
        } catch (Exception e) {
            showError("Failed to update profile. Please try again.");
        }
    }
    
    /**
     * Cancel profile editing and reload original data.
     */
    @FXML
    private void cancelEdit() {
        loadUserProfile();
        showInfo("Changes discarded.");
    }
    
    /**
     * Change user password.
     */
    @FXML
    private void changePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All password fields are required.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords do not match.");
            return;
        }
        
        if (newPassword.length() < 6) {
            showError("New password must be at least 6 characters long.");
            return;
        }
        
        if (newPassword.equals(currentPassword)) {
            showError("New password must be different from current password.");
            return;
        }
        
        // Attempt password change
        try {
            boolean success = authService.changePassword(currentUser.getId(), currentPassword, newPassword);
            
            if (success) {
                showSuccess("Password changed successfully!");
                
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                showError("Current password is incorrect.");
            }
        } catch (Exception e) {
            showError("Failed to change password: " + e.getMessage());
        }
    }
    
    /**
     * Navigate back to dashboard.
     */
    @FXML
    private void goBack() {
        try {
            Main.switchScene("Dashboard.fxml", "KnA - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to return to dashboard.");
        }
    }
    
    /**
     * Navigate to login page.
     */
    private void goToLogin() {
        try {
            SessionManager.getInstance().clearSession();
            Main.switchScene("Login.fxml", "KnA - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show success toast notification.
     */
    private void showSuccess(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.SUCCESS);
    }
    
    /**
     * Show error toast notification.
     */
    private void showError(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.ERROR);
    }
    
    /**
     * Show info toast notification.
     */
    private void showInfo(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.INFO);
    }
}
