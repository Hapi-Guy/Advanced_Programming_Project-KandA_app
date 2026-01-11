package com.kna.controller;

import com.kna.Main;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * RegisterController - Handles user registration
 */
public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    
    private final AuthService authService;

    public RegisterController() {
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        // Clear error when typing
        if (usernameField != null) {
            usernameField.textProperty().addListener((obs, old, newVal) -> clearError());
        }
        if (emailField != null) {
            emailField.textProperty().addListener((obs, old, newVal) -> clearError());
        }
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
        }
    }

    @FXML
    private void handleRegister() {
        // Get form data
        String username = usernameField != null ? usernameField.getText() : "";
        String email = emailField != null ? emailField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";
        
        // Clear previous error
        clearError();
        
        // Validate input
        if (username.trim().isEmpty()) {
            showError("Please enter a username");
            return;
        }
        
        if (email.trim().isEmpty()) {
            showError("Please enter your email");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        try {
            // Attempt registration with simplified parameters
            User user = authService.register(email, "", password, username, "General", 1);
            
            // Auto login
            authService.login(email, password);
            
            ToastNotification.showSuccess("Welcome to KnA, " + user.getName() + "!");
            
            // Navigate to dashboard
            javafx.application.Platform.runLater(() -> {
                Main.switchScene("/fxml/Dashboard.fxml", "KnA - Dashboard");
            });
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        // Navigate to login screen
        Main.switchScene("/fxml/Login.fxml", "KnA - Login");
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
}
