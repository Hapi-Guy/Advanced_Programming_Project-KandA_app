package com.kna.controller;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.kna.Main;
import com.kna.dao.QuestionDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.QuestionService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for My Questions page.
 * Displays all questions asked by the current user.
 */
public class MyQuestionsController {
    
    // Stats
    @FXML private Label totalQuestionsLabel;
    @FXML private Label answeredQuestionsLabel;
    @FXML private Label pendingQuestionsLabel;
    @FXML private Label totalCoinsSpentLabel;
    
    // Filter ComboBox
    @FXML private ComboBox<String> filterComboBox;
    
    // Content Areas
    @FXML private VBox questionsContainer;
    
    private QuestionDAO questionDAO;
    private QuestionService questionService;
    private User currentUser;
    private String currentFilter = "all";
    private List<Question> allQuestions;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        questionDAO = new QuestionDAO();
        questionService = new QuestionService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Initialize filter dropdown
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll("All", "Answered", "Pending", "Urgent");
            filterComboBox.setValue("All");
        }
        
        if (currentUser != null) {
            loadQuestions();
            loadStats();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Handle filter selection change from dropdown.
     */
    @FXML
    private void onFilterChanged() {
        if (filterComboBox == null || filterComboBox.getValue() == null) return;
        
        String selected = filterComboBox.getValue();
        currentFilter = switch (selected) {
            case "Answered" -> "answered";
            case "Pending" -> "pending";
            case "Urgent" -> "urgent";
            default -> "all";
        };
        
        displayQuestions(allQuestions);
    }
    
    /**
     * Load user's questions from database.
     */
    private void loadQuestions() {
        try {
            allQuestions = questionDAO.getQuestionsByUserId(currentUser.getId());
            displayQuestions(allQuestions);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load questions.");
        }
    }
    
    /**
     * Load and display statistics.
     */
    private void loadStats() {
        if (allQuestions == null) return;
        
        int total = allQuestions.size();
        long answered = allQuestions.stream().filter(Question::isAnswered).count();
        long pending = allQuestions.stream().filter(q -> !q.isAnswered()).count();
        int coinsSpent = allQuestions.stream()
            .mapToInt(q -> q.isUrgent() ? 30 : 20)
            .sum();
        
        if (totalQuestionsLabel != null) totalQuestionsLabel.setText(String.valueOf(total));
        if (answeredQuestionsLabel != null) answeredQuestionsLabel.setText(String.valueOf(answered));
        if (pendingQuestionsLabel != null) pendingQuestionsLabel.setText(String.valueOf(pending));
        if (totalCoinsSpentLabel != null) totalCoinsSpentLabel.setText(String.valueOf(coinsSpent));
    }
    
    /**
     * Display questions based on current filter.
     */
    private void displayQuestions(List<Question> questions) {
        questionsContainer.getChildren().clear();
        
        // Apply filter
        List<Question> filteredQuestions = switch (currentFilter) {
            case "answered" -> questions.stream().filter(Question::isAnswered).toList();
            case "pending" -> questions.stream().filter(q -> !q.isAnswered()).toList();
            case "urgent" -> questions.stream().filter(Question::isUrgent).toList();
            default -> questions;
        };
        
        if (filteredQuestions.isEmpty()) {
            // Show empty message
            Label emptyLabel = new Label("No questions found");
            emptyLabel.getStyleClass().add("empty-message");
            questionsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Question question : filteredQuestions) {
            VBox questionCard = createQuestionCard(question);
            questionsContainer.getChildren().add(questionCard);
        }
    }
    
    /**
     * Create a question card UI element.
     */
    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(12);
        card.getStyleClass().add("question-card");
        card.setPadding(new Insets(15, 20, 15, 20));
        
        // Header Row: Title + Badges
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label(question.getTitle());
        title.setWrapText(true);
        title.getStyleClass().add("question-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        // Badges
        HBox badges = new HBox(5);
        
        if (question.isUrgent()) {
            Label urgentBadge = new Label("URGENT");
            urgentBadge.getStyleClass().add("urgent-badge");
            badges.getChildren().add(urgentBadge);
        }
        
        if (question.isAnswered()) {
            Label answeredBadge = new Label("✓ Answered");
            answeredBadge.getStyleClass().add("answered-badge");
            badges.getChildren().add(answeredBadge);
        } else {
            Label unansweredBadge = new Label("Unanswered");
            unansweredBadge.getStyleClass().add("unanswered-badge");
            badges.getChildren().add(unansweredBadge);
        }
        
        if (!question.isEvaluated() && question.isAnswered()) {
            Label evalBadge = new Label("⚠ Needs Evaluation");
            evalBadge.getStyleClass().add("warning-badge");
            badges.getChildren().add(evalBadge);
        }
        
        headerRow.getChildren().addAll(title, badges);
        
        // Info Row
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        Label category = new Label("📚 " + question.getCategory());
        category.getStyleClass().add("question-meta");
        
        Label date = new Label("🕒 " + formatDate(question.getCreatedAt()));
        date.getStyleClass().add("question-meta");
        
        Label answerCount = new Label("💬 " + question.getAnswerCount() + " answers");
        answerCount.getStyleClass().add("question-meta");
        
        Label cost = new Label("💰 " + (question.isUrgent() ? "30" : "20") + " coins");
        cost.getStyleClass().add("question-meta");
        
        Label views = new Label("👁 " + question.getViews() + " views");
        views.getStyleClass().add("question-meta");
        
        infoRow.getChildren().addAll(category, date, answerCount, cost, views);
        
        // Action Row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("secondary-button");
        viewBtn.setOnAction(e -> viewQuestion(question));
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteQuestion(question));
        
        actionRow.getChildren().addAll(viewBtn, deleteBtn);
        
        card.getChildren().addAll(headerRow, infoRow, actionRow);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(""));
        
        return card;
    }
    
    /**
     * Format date for display.
     */
    private String formatDate(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "Unknown";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return timestamp.toLocalDateTime().format(formatter);
    }
    
    /**
     * View question details.
     */
    private void viewQuestion(Question question) {
        try {
            // Try to find the Dashboard's content area (when loaded inside Dashboard)
            StackPane dashboardContentArea = findDashboardContentArea();
            
            if (dashboardContentArea != null) {
                // Load into Dashboard's content area
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionDetail.fxml"));
                Parent questionView = loader.load();
                
                QuestionDetailController controller = loader.getController();
                controller.loadQuestion(question.getId());
                
                dashboardContentArea.getChildren().clear();
                dashboardContentArea.getChildren().add(questionView);
            } else {
                // Fallback to switching scene
                SessionManager.getInstance().setAttribute("viewQuestionId", question.getId());
                Main.switchScene("/fxml/QuestionDetail.fxml", "KnA - Question Details");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open question.");
        }
    }
    
    /**
     * Delete a question with confirmation dialog.
     */
    private void deleteQuestion(Question question) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Question");
        confirmDialog.setHeaderText("Are you sure you want to delete this question?");
        confirmDialog.setContentText("\"" + question.getTitle() + "\"\n\n" +
            "⚠️ This will also delete all answers to this question.\n" +
            "This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = questionService.deleteQuestion(question.getId());
                if (deleted) {
                    ToastNotification.show("Question deleted successfully", ToastNotification.NotificationType.SUCCESS);
                    // Reload questions
                    loadQuestions();
                    loadStats();
                } else {
                    showError("Failed to delete question.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
    }
    
    /**
     * Find the Dashboard's content area by traversing up the scene graph.
     */
    private StackPane findDashboardContentArea() {
        try {
            javafx.scene.Node node = questionsContainer;
            while (node != null) {
                if (node instanceof StackPane && node.getId() != null && node.getId().equals("contentArea")) {
                    return (StackPane) node;
                }
                node = node.getParent();
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }
    
    /**
     * Navigate to Ask Question page.
     */
    @FXML
    private void askQuestion() {
        try {
            Main.switchScene("/fxml/AskQuestion.fxml", "KnA - Ask Question");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Ask Question page.");
        }
    }
    
    /**
     * Navigate back to dashboard.
     */
    @FXML
    private void goBack() {
        try {
            Main.switchScene("/fxml/Dashboard.fxml", "KnA - Dashboard");
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
            Main.switchScene("/fxml/Login.fxml", "KnA - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show error toast notification.
     */
    private void showError(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.ERROR);
    }
}
