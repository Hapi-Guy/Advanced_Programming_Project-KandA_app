package com.kna.controller;

import com.kna.Main;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * RegisterController - Handles user registration
 */
public class RegisterController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    
    private final AuthService authService;

    public RegisterController() {
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        // Populate department ComboBox
        departmentComboBox.getItems().addAll(
            "CSE", "EEE", "CE", "ME", "IPE", "TE", "GCE", "URP", "ARCH", "Other"
        );
        
        // Populate year ComboBox
        yearComboBox.getItems().addAll("1", "2", "3", "4", "5");
        
        // Clear messages when typing
        nameField.textProperty().addListener((obs, old, newVal) -> clearMessages());
        emailField.textProperty().addListener((obs, old, newVal) -> clearMessages());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearMessages());
    }

    @FXML
    private void handleRegister() {
        // Get form data
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String department = departmentComboBox.getValue();
        String yearStr = yearComboBox.getValue();
        
        // Clear previous messages
        clearMessages();
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            showError("Please enter your full name");
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            showError("Please enter your email");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            showError("Please enter a password");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (department == null) {
            showError("Please select your department");
            return;
        }
        
        if (yearStr == null) {
            showError("Please select your academic year");
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            showError("Invalid academic year");
            return;
        }
        
        // Disable register button
        registerButton.setDisable(true);
        
        try {
            // Attempt registration
            User user = authService.register(email, phone, password, name, department, year);
            
            // Show success message
            showSuccess("Registration successful! Logging you in...");
            
            // Auto login
            authService.login(email, password);
            
            ToastNotification.showSuccess("Welcome to KnA, " + user.getName() + "!");
            
            // Navigate to dashboard
            javafx.application.Platform.runLater(() -> {
                Main.switchScene("/fxml/Dashboard.fxml", "KnA - Dashboard");
            });
            
        } catch (Exception e) {
            showError(e.getMessage());
            registerButton.setDisable(false);
        }
    }

    @FXML
    private void handleLogin() {
        // Navigate to login screen
        Main.switchScene("/fxml/Login.fxml", "KnA - Login");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
}
