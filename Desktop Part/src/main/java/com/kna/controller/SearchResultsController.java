package com.kna.controller;

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
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Search Results page.
 * Displays search results for questions and users.
 */
public class SearchResultsController {
    
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Label resultsCountLabel;
    
    // Filter Tabs
    @FXML private Button allTabBtn;
    @FXML private Button questionsTabBtn;
    @FXML private Button usersTabBtn;
    
    // Content Areas
    @FXML private ScrollPane resultsScrollPane;
    @FXML private VBox resultsList;
    @FXML private StackPane emptyStatePane;
    @FXML private StackPane initialStatePane;
    @FXML private Label emptyStateTitle;
    @FXML private Label emptyStateSubtitle;
    
    private QuestionDAO questionDAO;
    private UserDAO userDAO;
    private User currentUser;
    private String currentFilter = "all";
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
            searchField.setText(searchQuery);
            performSearch();
            SessionManager.getInstance().removeAttribute("searchQuery");
        }
        
        // Enter key handler
        searchField.setOnAction(e -> performSearch());
    }
    
    /**
     * Perform search based on input.
     */
    @FXML
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            showWarning("Please enter a search query.");
            return;
        }
        
        if (query.length() < 2) {
            showWarning("Search query must be at least 2 characters.");
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
        resultsList.getChildren().clear();
        hideInitialState();
        
        int totalResults = foundQuestions.size() + foundUsers.size();
        
        if (totalResults == 0) {
            showEmptyState("No results found", "Try different keywords or check your spelling");
            resultsCountLabel.setText("Found 0 results");
            return;
        }
        
        hideEmptyState();
        
        // Update results count
        resultsCountLabel.setText("Found " + totalResults + " result" + (totalResults == 1 ? "" : "s"));
        
        // Display based on filter
        switch (currentFilter) {
            case "questions":
                displayQuestionResults();
                break;
            case "users":
                displayUserResults();
                break;
            default:
                displayQuestionResults();
                if (!foundQuestions.isEmpty() && !foundUsers.isEmpty()) {
                    resultsList.getChildren().add(createSectionDivider("Users"));
                }
                displayUserResults();
                break;
        }
    }
    
    /**
     * Display question results.
     */
    private void displayQuestionResults() {
        if (foundQuestions.isEmpty()) return;
        
        if (currentFilter.equals("all")) {
            resultsList.getChildren().add(createSectionDivider("Questions (" + foundQuestions.size() + ")"));
        }
        
        for (Question question : foundQuestions) {
            VBox questionCard = createQuestionCard(question);
            resultsList.getChildren().add(questionCard);
        }
    }
    
    /**
     * Display user results.
     */
    private void displayUserResults() {
        if (foundUsers.isEmpty()) return;
        
        if (currentFilter.equals("all")) {
            resultsList.getChildren().add(createSectionDivider("Users (" + foundUsers.size() + ")"));
        }
        
        for (User user : foundUsers) {
            HBox userCard = createUserCard(user);
            resultsList.getChildren().add(userCard);
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
     * Filter: Show all results.
     */
    @FXML
    private void showAll() {
        currentFilter = "all";
        updateActiveTab(allTabBtn);
        displayResults();
    }
    
    /**
     * Filter: Show questions only.
     */
    @FXML
    private void showQuestions() {
        currentFilter = "questions";
        updateActiveTab(questionsTabBtn);
        displayResults();
    }
    
    /**
     * Filter: Show users only.
     */
    @FXML
    private void showUsers() {
        currentFilter = "users";
        updateActiveTab(usersTabBtn);
        displayResults();
    }
    
    /**
     * Update active tab.
     */
    private void updateActiveTab(Button activeButton) {
        allTabBtn.getStyleClass().remove("active-tab");
        questionsTabBtn.getStyleClass().remove("active-tab");
        usersTabBtn.getStyleClass().remove("active-tab");
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Show empty state.
     */
    private void showEmptyState(String title, String subtitle) {
        resultsScrollPane.setManaged(false);
        resultsScrollPane.setVisible(false);
        emptyStatePane.setManaged(true);
        emptyStatePane.setVisible(true);
        emptyStateTitle.setText(title);
        emptyStateSubtitle.setText(subtitle);
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        resultsScrollPane.setManaged(true);
        resultsScrollPane.setVisible(true);
        emptyStatePane.setManaged(false);
        emptyStatePane.setVisible(false);
    }
    
    /**
     * Hide initial state.
     */
    private void hideInitialState() {
        initialStatePane.setManaged(false);
        initialStatePane.setVisible(false);
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
     * Show warning toast.
     */
    private void showWarning(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.WARNING);
    }
    
    /**
     * Show error toast.
     */
    private void showError(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.ERROR);
    }
}
