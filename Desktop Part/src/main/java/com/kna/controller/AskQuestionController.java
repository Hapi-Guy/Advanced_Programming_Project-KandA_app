package com.kna.controller;

import com.kna.Main;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.QuestionService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for the Ask Question page.
 * Allows users to post new questions with optional images and urgent mode.
 */
public class AskQuestionController {
    
    @FXML private Label coinBalanceLabel;
    @FXML private Label errorLabel;
    
    // Form Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox urgentCheckbox;
    @FXML private javafx.scene.control.Slider rewardSlider;
    @FXML private Label rewardLabel;
    
    // Cost summary labels
    @FXML private Label baseRewardSummary;
    @FXML private Label urgentFeeSummary;
    @FXML private Label totalCostLabel;
    
    private QuestionService questionService;
    private User currentUser;
    private int currentReward = 5;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        questionService = new QuestionService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Populate category ComboBox
        categoryComboBox.getItems().addAll(
            "CSE", "EEE", "ECE", "MTE", "CE", "ME", "IEM", "TE", "Arch", "URP", 
            "BME", "MSE", "LE", "ESE", "BECM", "ChE", "MATH", "HUM", "PHY", "CHEM", "General"
        );
        
        // Setup reward slider listener
        if (rewardSlider != null) {
            rewardSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentReward = newVal.intValue();
                if (rewardLabel != null) {
                    rewardLabel.setText(String.valueOf(currentReward));
                }
                updateCostSummary();
            });
        }
        
        // Setup urgent checkbox listener
        if (urgentCheckbox != null) {
            urgentCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateCostSummary();
            });
        }
        
        if (currentUser != null) {
            loadUserData();
            updateCostSummary();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load user's current coin balance.
     */
    private void loadUserData() {
        if (coinBalanceLabel != null) {
            coinBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
        }
    }
    
    /**
     * Update the cost summary display.
     */
    private void updateCostSummary() {
        int baseReward = currentReward;
        int urgentFee = (urgentCheckbox != null && urgentCheckbox.isSelected()) ? 5 : 0;
        int total = baseReward + urgentFee;
        
        if (baseRewardSummary != null) baseRewardSummary.setText(baseReward + " coins");
        if (urgentFeeSummary != null) urgentFeeSummary.setText(urgentFee + " coins");
        if (totalCostLabel != null) totalCostLabel.setText(total + " coins");
    }
    
    /**
     * Submit the question to the database.
     */
    @FXML
    private void handleSubmit() {
        // Validate inputs
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = categoryComboBox.getValue();
        boolean isUrgent = urgentCheckbox != null && urgentCheckbox.isSelected();
        
        // Clear previous error
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        if (title.isEmpty()) {
            showFormError("Please enter a question title.");
            return;
        }
        
        if (title.length() < 10) {
            showFormError("Title must be at least 10 characters long.");
            return;
        }
        
        if (description.isEmpty()) {
            showFormError("Please provide a detailed description.");
            return;
        }
        
        if (description.length() < 20) {
            showFormError("Description must be at least 20 characters long.");
            return;
        }
        
        if (category == null || category.isEmpty()) {
            showFormError("Please select a category.");
            return;
        }
        
        // Calculate cost
        int urgentFee = isUrgent ? 5 : 0;
        int totalCost = currentReward + urgentFee;
        
        // Check if user has enough coins
        if (currentUser.getCoins() < totalCost) {
            showFormError("Insufficient coins! You need " + totalCost + " coins to post this question.");
            return;
        }
        
        try {
            // Post question using QuestionService
            Question question = questionService.askQuestion(
                title,
                description,
                category,
                isUrgent,
                null // No image file in simplified form
            );
            
            if (question != null && question.getQuestionId() > 0) {
                // Update current user's coin balance
                currentUser = SessionManager.getInstance().getCurrentUser();
                
                showSuccess("Question posted successfully! " + totalCost + " coins deducted.");
                
                // Clear all fields after successful submission
                clearAllFields();
            } else {
                showFormError("Failed to post question. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showFormError("Error: " + e.getMessage());
        }
    }
    
    /**
     * Show error in the form error label.
     */
    private void showFormError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #f44336;");
        }
        showError(message);
    }
    
    /**
     * Clear all form fields.
     */
    private void clearAllFields() {
        titleField.clear();
        descriptionArea.clear();
        categoryComboBox.setValue(null);
        if (urgentCheckbox != null) urgentCheckbox.setSelected(false);
        if (rewardSlider != null) rewardSlider.setValue(5);
        currentReward = 5;
        updateCostSummary();
        if (errorLabel != null) errorLabel.setText("");
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
}
