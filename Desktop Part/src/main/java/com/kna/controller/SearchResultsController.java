package com.kna.controller;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.kna.Main;
import com.kna.dao.QuestionDAO;
import com.kna.dao.UserDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for Search Results page.
 * Displays search results for questions and users.
 */
public class SearchResultsController {
    
    @FXML private Label searchQueryLabel;
    @FXML private Label resultCountLabel;
    @FXML private VBox resultsContainer;
    @FXML private VBox emptyState;
    
    private QuestionDAO questionDAO;
    private UserDAO userDAO;
    private User currentUser;
    private List<Question> foundQuestions = new ArrayList<>();
    private List<User> foundUsers = new ArrayList<>();
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        questionDAO = new QuestionDAO();
        userDAO = new UserDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            showError("Session expired. Please login again.");
            goToLogin();
            return;
        }
        
        // Get search query from session if navigated from dashboard
        String searchQuery = (String) SessionManager.getInstance().getAttribute("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            if (searchQueryLabel != null) {
                searchQueryLabel.setText("Results for: \"" + searchQuery + "\"");
            }
            performSearch(searchQuery);
            SessionManager.getInstance().removeAttribute("searchQuery");
        }
    }
    
    /**
     * Perform search based on input.
     */
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        try {
            // Search questions
            foundQuestions = questionDAO.searchQuestions(query);
            
            // Search users
            foundUsers = userDAO.searchUsers(query);
            
            // Display results
            displayResults();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Search failed. Please try again.");
        }
    }
    
    /**
     * Display search results based on current filter.
     */
    private void displayResults() {
        resultsContainer.getChildren().clear();
        
        int totalResults = foundQuestions.size() + foundUsers.size();
        
        if (totalResults == 0) {
            showEmptyState();
            resultCountLabel.setText("0 results");
            return;
        }
        
        hideEmptyState();
        
        // Update results count
        resultCountLabel.setText(totalResults + " result" + (totalResults == 1 ? "" : "s"));
        
        // Display questions
        displayQuestionResults();
        
        // Display users if any
        if (!foundUsers.isEmpty()) {
            resultsContainer.getChildren().add(createSectionDivider("Users"));
            displayUserResults();
        }
    }
    
    /**
     * Display question results.
     */
    private void displayQuestionResults() {
        if (foundQuestions.isEmpty()) return;
        
        resultsContainer.getChildren().add(createSectionDivider("Questions (" + foundQuestions.size() + ")"));
        
        for (Question question : foundQuestions) {
            VBox questionCard = createQuestionCard(question);
            resultsContainer.getChildren().add(questionCard);
        }
    }
    
    /**
     * Display user results.
     */
    private void displayUserResults() {
        if (foundUsers.isEmpty()) return;
        
        for (User user : foundUsers) {
            HBox userCard = createUserCard(user);
            resultsContainer.getChildren().add(userCard);
        }
    }
    
    /**
     * Create section divider.
     */
    private HBox createSectionDivider(String title) {
        HBox divider = new HBox();
        divider.setPadding(new Insets(10, 0, 10, 0));
        
        Label label = new Label(title);
        label.getStyleClass().add("section-header");
        divider.getChildren().add(label);
        
        return divider;
    }
    
    /**
     * Create question card.
     */
    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.getStyleClass().add("question-card");
        card.setPadding(new Insets(15, 20, 15, 20));
        
        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
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
        }
        
        titleRow.getChildren().addAll(title, badges);
        
        // Description preview
        String desc = question.getDescription();
        if (desc.length() > 150) {
            desc = desc.substring(0, 147) + "...";
        }
        Label description = new Label(desc);
        description.setWrapText(true);
        description.getStyleClass().add("question-description");
        
        // Meta info
        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        
        Label category = new Label("📚 " + question.getCategory());
        category.getStyleClass().add("question-meta");
        
        Label answers = new Label("💬 " + question.getAnswerCount() + " answers");
        answers.getStyleClass().add("question-meta");
        
        Label views = new Label("👁 " + question.getViews() + " views");
        views.getStyleClass().add("question-meta");
        
        Label date = new Label("🕒 " + formatDate(question.getCreatedAt()));
        date.getStyleClass().add("question-meta");
        
        metaRow.getChildren().addAll(category, answers, views, date);
        
        // Action row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewBtn = new Button("View Question");
        viewBtn.getStyleClass().add("secondary-button");
        viewBtn.setOnAction(e -> viewQuestion(question));
        actionRow.getChildren().add(viewBtn);
        
        card.getChildren().addAll(titleRow, description, metaRow, actionRow);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(""));
        
        return card;
    }
    
    /**
     * Create user card.
     */
    private HBox createUserCard(User user) {
        HBox card = new HBox(15);
        card.getStyleClass().add("user-card");
        card.setPadding(new Insets(12, 20, 12, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar
        StackPane avatarPane = new StackPane();
        avatarPane.getStyleClass().add("avatar-medium");
        Label avatarLabel = new Label(getInitials(user.getName()));
        avatarLabel.getStyleClass().add("avatar-text-medium");
        avatarPane.getChildren().add(avatarLabel);
        
        // User info
        VBox userInfo = new VBox(5);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        
        Label name = new Label(user.getName());
        name.getStyleClass().add("user-name");
        
        HBox stats = new HBox(15);
        stats.setAlignment(Pos.CENTER_LEFT);
        
        Label dept = new Label("📚 " + user.getDepartment());
        dept.getStyleClass().add("user-meta");
        
        Label reputation = new Label("⭐ " + user.getReputation() + " reputation");
        reputation.getStyleClass().add("user-meta");
        
        Label answers = new Label("💬 " + user.getAnswersGiven() + " answers");
        answers.getStyleClass().add("user-meta");
        
        stats.getChildren().addAll(dept, reputation, answers);
        userInfo.getChildren().addAll(name, stats);
        
        card.getChildren().addAll(avatarPane, userInfo);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(""));
        
        return card;
    }
    
    /**
     * Get initials from name.
     */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    
    /**
     * Format date.
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
     * Navigate back.
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
     * Show empty state.
     */
    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setManaged(true);
            emptyState.setVisible(true);
        }
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setManaged(false);
            emptyState.setVisible(false);
        }
    }
    
    /**
     * Navigate to login.
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
     * Show error toast.
     */
    private void showError(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.ERROR);
    }
}
