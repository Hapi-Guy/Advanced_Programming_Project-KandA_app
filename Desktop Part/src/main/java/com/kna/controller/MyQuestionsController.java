package com.kna.controller;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.kna.Main;
import com.kna.dao.QuestionDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for My Questions page.
 * Displays all questions asked by the current user.
 */
public class MyQuestionsController {
    
    @FXML private Button backButton;
    
    // Stats
    @FXML private Label totalQuestionsLabel;
    @FXML private Label answeredLabel;
    @FXML private Label unevaluatedLabel;
    @FXML private Label coinsSpentLabel;
    
    // Filter Buttons
    @FXML private Button allBtn;
    @FXML private Button answeredBtn;
    @FXML private Button unansweredBtn;
    @FXML private Button urgentBtn;
    
    // Content Areas
    @FXML private ScrollPane questionsScrollPane;
    @FXML private VBox questionsList;
    @FXML private StackPane emptyStatePane;
    
    private QuestionDAO questionDAO;
    private User currentUser;
    private String currentFilter = "all";
    private List<Question> allQuestions;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        questionDAO = new QuestionDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            loadQuestions();
            loadStats();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
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
        long unevaluated = allQuestions.stream().filter(q -> !q.isEvaluated()).count();
        int coinsSpent = allQuestions.stream()
            .mapToInt(q -> q.isUrgent() ? 30 : 20)
            .sum();
        
        totalQuestionsLabel.setText(String.valueOf(total));
        answeredLabel.setText(String.valueOf(answered));
        unevaluatedLabel.setText(String.valueOf(unevaluated));
        coinsSpentLabel.setText(String.valueOf(coinsSpent));
    }
    
    /**
     * Display questions based on current filter.
     */
    private void displayQuestions(List<Question> questions) {
        questionsList.getChildren().clear();
        
        // Apply filter
        List<Question> filteredQuestions = switch (currentFilter) {
            case "answered" -> questions.stream().filter(Question::isAnswered).toList();
            case "unanswered" -> questions.stream().filter(q -> !q.isAnswered()).toList();
            case "urgent" -> questions.stream().filter(Question::isUrgent).toList();
            default -> questions;
        };
        
        if (filteredQuestions.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        
        for (Question question : filteredQuestions) {
            VBox questionCard = createQuestionCard(question);
            questionsList.getChildren().add(questionCard);
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
        
        actionRow.getChildren().add(viewBtn);
        
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
            SessionManager.getInstance().setAttribute("viewQuestionId", question.getId());
            Main.switchScene("QuestionDetail.fxml", "KnA - Question Details");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open question.");
        }
    }
    
    /**
     * Filter: Show all questions.
     */
    @FXML
    private void showAllQuestions() {
        currentFilter = "all";
        updateActiveTab(allBtn);
        displayQuestions(allQuestions);
    }
    
    /**
     * Filter: Show answered questions.
     */
    @FXML
    private void showAnsweredQuestions() {
        currentFilter = "answered";
        updateActiveTab(answeredBtn);
        displayQuestions(allQuestions);
    }
    
    /**
     * Filter: Show unanswered questions.
     */
    @FXML
    private void showUnansweredQuestions() {
        currentFilter = "unanswered";
        updateActiveTab(unansweredBtn);
        displayQuestions(allQuestions);
    }
    
    /**
     * Filter: Show urgent questions.
     */
    @FXML
    private void showUrgentQuestions() {
        currentFilter = "urgent";
        updateActiveTab(urgentBtn);
        displayQuestions(allQuestions);
    }
    
    /**
     * Update active tab styling.
     */
    private void updateActiveTab(Button activeButton) {
        allBtn.getStyleClass().remove("active-tab");
        answeredBtn.getStyleClass().remove("active-tab");
        unansweredBtn.getStyleClass().remove("active-tab");
        urgentBtn.getStyleClass().remove("active-tab");
        
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Navigate to Ask Question page.
     */
    @FXML
    private void askQuestion() {
        try {
            Main.switchScene("AskQuestion.fxml", "KnA - Ask Question");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Ask Question page.");
        }
    }
    
    /**
     * Show empty state.
     */
    private void showEmptyState() {
        questionsScrollPane.setManaged(false);
        questionsScrollPane.setVisible(false);
        emptyStatePane.setManaged(true);
        emptyStatePane.setVisible(true);
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        questionsScrollPane.setManaged(true);
        questionsScrollPane.setVisible(true);
        emptyStatePane.setManaged(false);
        emptyStatePane.setVisible(false);
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
     * Show error toast notification.
     */
    private void showError(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.ERROR);
    }
}
