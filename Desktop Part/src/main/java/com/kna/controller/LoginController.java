package com.kna.controller;

import com.kna.Main;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;


public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;
    @FXML private RadioButton userRadioButton;
    @FXML private RadioButton adminRadioButton;
    
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        // Add enter key handler
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            showError("Please enter your email");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            showError("Please enter your password");
            return;
        }
        
        // Disable login button
        loginButton.setDisable(true);
        
        // Determine selected login type
        boolean isAdminLogin = adminRadioButton.isSelected();
        
        try {
            // Attempt login
            User user = authService.login(email, password);
            
            // Validate login type matches user role
            if (isAdminLogin && !user.isAdmin()) {
                // Admin login selected but user is not admin
                showError("Invalid email or password");
                loginButton.setDisable(false);
                return;
            }
            
            if (!isAdminLogin && user.isAdmin()) {
                // User login selected but account is admin
                showError("Invalid email or password");
                loginButton.setDisable(false);
                return;
            }
            
            // Show success message
            ToastNotification.showSuccess("Welcome back, " + user.getName() + "!");
            
            // Navigate to dashboard
            Main.switchScene("/fxml/Dashboard.fxml", "KnA - Dashboard");
            
        } catch (Exception e) {
            showError(e.getMessage());
            loginButton.setDisable(false);
        }
    }

    @FXML
    private void handleRegister() {
        // Navigate to register screen
        Main.switchScene("/fxml/Register.fxml", "KnA - Register");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
