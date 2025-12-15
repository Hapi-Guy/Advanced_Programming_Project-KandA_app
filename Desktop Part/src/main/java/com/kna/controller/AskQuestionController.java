package com.kna.controller;

import com.kna.Main;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.QuestionService;
import com.kna.util.ImageLoader;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for the Ask Question page.
 * Allows users to post new questions with optional images and urgent mode.
 */
public class AskQuestionController {
    
    @FXML private Button backButton;
    @FXML private Label coinsLabel;
    
    // Warning Box
    @FXML private HBox warningBox;
    @FXML private Label warningLabel;
    @FXML private Label warningSubLabel;
    
    // Form Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private CheckBox urgentCheckbox;
    @FXML private Label costLabel;
    @FXML private Label imageFileLabel;
    @FXML private StackPane imagePreviewPane;
    @FXML private ImageView imagePreview;
    
    private QuestionService questionService;
    private User currentUser;
    private File selectedImageFile;
    
    private static final int BASE_COST = 20;
    private static final int URGENT_COST = 30;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        questionService = new QuestionService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Populate category ComboBox
        categoryCombo.getItems().addAll(
            "CSE", "EEE", "CE", "ME", "IPE", "TE", "NAME", "Arch", "URP", 
            "BME", "MSE", "GCE", "WRE", "BECM", "General"
        );
        
        if (currentUser != null) {
            loadUserData();
            checkUnevaluatedQuestions();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load user's current coin balance.
     */
    private void loadUserData() {
        coinsLabel.setText(String.valueOf(currentUser.getCoins()));
    }
    
    /**
     * Check if user has too many unevaluated questions.
     */
    private void checkUnevaluatedQuestions() {
        try {
            boolean canAsk = questionService.canAskQuestion(currentUser.getId());
            
            if (!canAsk) {
                int unevaluatedCount = questionService.getUnevaluatedCount(currentUser.getId());
                warningBox.setManaged(true);
                warningBox.setVisible(true);
                warningLabel.setText("You have " + unevaluatedCount + " unevaluated questions!");
                warningSubLabel.setText("Please evaluate answered questions before posting new ones. Maximum allowed: 5 unevaluated questions.");
                
                // Disable form
                titleField.setDisable(true);
                descriptionArea.setDisable(true);
                categoryCombo.setDisable(true);
                urgentCheckbox.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update the cost display when urgent checkbox is toggled.
     */
    @FXML
    private void updateCostDisplay() {
        int cost = urgentCheckbox.isSelected() ? URGENT_COST : BASE_COST;
        costLabel.setText(String.valueOf(cost));
    }
    
    /**
     * Choose an image file to attach to the question.
     */
    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Question Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(backButton.getScene().getWindow());
        
        if (file != null) {
            selectedImageFile = file;
            imageFileLabel.setText(file.getName());
            
            // Show preview
            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreviewPane.setManaged(true);
                imagePreviewPane.setVisible(true);
            } catch (Exception e) {
                showError("Failed to load image preview.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Submit the question to the database.
     */
    @FXML
    private void submitQuestion() {
        // Validate inputs
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = categoryCombo.getValue();
        boolean isUrgent = urgentCheckbox.isSelected();
        
        if (title.isEmpty()) {
            showError("Please enter a question title.");
            return;
        }
        
        if (title.length() < 10) {
            showError("Title must be at least 10 characters long.");
            return;
        }
        
        if (description.isEmpty()) {
            showError("Please provide a detailed description.");
            return;
        }
        
        if (description.length() < 20) {
            showError("Description must be at least 20 characters long.");
            return;
        }
        
        if (category == null || category.isEmpty()) {
            showError("Please select a category.");
            return;
        }
        
        // Check if user can ask question
        try {
            if (!questionService.canAskQuestion(currentUser.getId())) {
                showError("You have too many unevaluated questions. Please evaluate answered questions first.");
                return;
            }
            
            // Check if user has enough coins
            int cost = isUrgent ? URGENT_COST : BASE_COST;
            if (currentUser.getCoins() < cost) {
                showError("Insufficient coins! You need " + cost + " coins to post this question.");
                return;
            }
            
            // Create question object
            Question question = new Question();
            question.setUserId(currentUser.getId());
            question.setTitle(title);
            question.setDescription(description);
            question.setCategory(category);
            question.setUrgent(isUrgent);
            
            // Save image if selected
            String imagePath = null;
            if (selectedImageFile != null) {
                imagePath = ImageLoader.saveQuestionImage(selectedImageFile);
            }
            
            // Post question
            int questionId = questionService.askQuestion(question, imagePath);
            
            if (questionId > 0) {
                // Update current user's coin balance
                currentUser = SessionManager.getInstance().getCurrentUser(); // Refresh from session
                
                showSuccess("Question posted successfully! " + cost + " coins deducted.");
                
                // Navigate back to dashboard
                goBack();
            } else {
                showError("Failed to post question. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: " + e.getMessage());
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
}
