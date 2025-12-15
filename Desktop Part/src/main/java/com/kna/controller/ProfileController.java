package com.kna.controller;

import com.kna.Main;
import com.kna.dao.UserDAO;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Controller for the user profile page.
 * Displays user information, statistics, and allows editing profile details and password.
 */
public class ProfileController {
    
    @FXML private Button backButton;
    
    // Profile Header
    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label coinsLabel;
    @FXML private Label reputationLabel;
    
    // Statistics
    @FXML private Label questionsAskedLabel;
    @FXML private Label answersGivenLabel;
    @FXML private Label acceptedAnswersLabel;
    @FXML private Label acceptanceRateLabel;
    
    // Editable Fields
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private ComboBox<String> yearCombo;
    
    // Password Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
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
        
        // Populate department ComboBox
        departmentCombo.getItems().addAll(
            "CSE", "EEE", "CE", "ME", "IPE", "TE", "NAME", "Arch", "URP", 
            "BME", "MSE", "GCE", "WRE", "BECM"
        );
        
        // Populate year ComboBox
        yearCombo.getItems().addAll("1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate");
        
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
        currentUser = userDAO.findById(currentUser.getId());
        if (currentUser == null) {
            showError("Failed to load user data.");
            return;
        }
        
        // Update session
        SessionManager.getInstance().setCurrentUser(currentUser);
        
        // Display profile header
        String initials = getInitials(currentUser.getName());
        avatarLabel.setText(initials);
        nameLabel.setText(currentUser.getName());
        emailLabel.setText(currentUser.getEmail());
        coinsLabel.setText(String.valueOf(currentUser.getCoins()));
        reputationLabel.setText(String.valueOf(currentUser.getReputation()));
        
        // Display statistics
        questionsAskedLabel.setText(String.valueOf(currentUser.getQuestionsAsked()));
        answersGivenLabel.setText(String.valueOf(currentUser.getAnswersGiven()));
        acceptedAnswersLabel.setText(String.valueOf(currentUser.getAcceptedAnswers()));
        
        double acceptanceRate = currentUser.getAcceptanceRate();
        acceptanceRateLabel.setText(String.format("%.1f%%", acceptanceRate));
        
        // Populate editable fields
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());
        departmentCombo.setValue(currentUser.getDepartment());
        yearCombo.setValue(currentUser.getAcademicYear());
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
    private void saveProfile() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String department = departmentCombo.getValue();
        String year = yearCombo.getValue();
        
        // Validation
        if (name.isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }
        
        if (department == null || department.isEmpty()) {
            showError("Please select a department.");
            return;
        }
        
        if (year == null || year.isEmpty()) {
            showError("Please select an academic year.");
            return;
        }
        
        // Update user object
        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setDepartment(department);
        currentUser.setAcademicYear(year);
        
        // Save to database
        boolean success = userDAO.updateUser(currentUser);
        
        if (success) {
            SessionManager.getInstance().setCurrentUser(currentUser);
            showSuccess("Profile updated successfully!");
            loadUserProfile(); // Refresh display
        } else {
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
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.SUCCESS);
    }
    
    /**
     * Show error toast notification.
     */
    private void showError(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.ERROR);
    }
    
    /**
     * Show info toast notification.
     */
    private void showInfo(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.INFO);
    }
}
