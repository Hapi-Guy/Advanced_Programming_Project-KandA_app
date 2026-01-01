package com.kna.controller;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.kna.Main;
import com.kna.dao.AnswerDAO;
import com.kna.dao.QuestionDAO;
import com.kna.model.Answer;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for My Answers page.
 * Displays all answers submitted by the current user.
 */
public class MyAnswersController {
    
    @FXML private Button backButton;
    
    // Stats
    @FXML private Label totalAnswersLabel;
    @FXML private Label acceptedLabel;
    @FXML private Label acceptanceRateLabel;
    @FXML private Label coinsEarnedLabel;
    @FXML private Label totalVotesLabel;
    
    // Filter Buttons
    @FXML private Button allBtn;
    @FXML private Button acceptedBtn;
    @FXML private Button topRatedBtn;
    
    // Content Areas
    @FXML private ScrollPane answersScrollPane;
    @FXML private VBox answersList;
    @FXML private StackPane emptyStatePane;
    
    private AnswerDAO answerDAO;
    private QuestionDAO questionDAO;
    private User currentUser;
    private String currentFilter = "all";
    private List<Answer> allAnswers;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        answerDAO = new AnswerDAO();
        questionDAO = new QuestionDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            loadAnswers();
            loadStats();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load user's answers from database.
     */
    private void loadAnswers() {
        try {
            allAnswers = answerDAO.getAnswersByUserId(currentUser.getId());
            displayAnswers(allAnswers);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load answers.");
        }
    }
    
    /**
     * Load and display statistics.
     */
    private void loadStats() {
        if (allAnswers == null) return;
        
        int total = allAnswers.size();
        long accepted = allAnswers.stream().filter(Answer::isAccepted).count();
        double acceptanceRate = total > 0 ? (accepted * 100.0 / total) : 0;
        int coinsEarned = allAnswers.stream()
            .filter(Answer::isAccepted)
            .mapToInt(Answer::getCoinsAwarded)
            .sum();
        int totalVotes = allAnswers.stream()
            .mapToInt(a -> a.getUpvotes() - a.getDownvotes())
            .sum();
        
        totalAnswersLabel.setText(String.valueOf(total));
        acceptedLabel.setText(String.valueOf(accepted));
        acceptanceRateLabel.setText(String.format("%.1f%%", acceptanceRate));
        coinsEarnedLabel.setText(String.valueOf(coinsEarned));
        totalVotesLabel.setText(String.valueOf(totalVotes));
    }
    
    /**
     * Display answers based on current filter.
     */
    private void displayAnswers(List<Answer> answers) {
        answersList.getChildren().clear();
        
        // Apply filter
        List<Answer> filteredAnswers = switch (currentFilter) {
            case "accepted" -> answers.stream().filter(Answer::isAccepted).toList();
            case "topRated" -> answers.stream()
                .filter(a -> a.getUpvotes() - a.getDownvotes() >= 5)
                .sorted((a, b) -> Integer.compare(b.getUpvotes() - b.getDownvotes(), a.getUpvotes() - a.getDownvotes()))
                .toList();
            default -> answers;
        };
        
        if (filteredAnswers.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        
        for (Answer answer : filteredAnswers) {
            VBox answerCard = createAnswerCard(answer);
            answersList.getChildren().add(answerCard);
        }
    }
    
    /**
     * Create an answer card UI element.
     */
    private VBox createAnswerCard(Answer answer) {
        VBox card = new VBox(12);
        card.getStyleClass().add("answer-card");
        card.setPadding(new Insets(15, 20, 15, 20));
        
        // Get question title
        String questionTitle = "Loading...";
        try {
            Question question = questionDAO.findById(answer.getQuestionId());
            if (question != null) {
                questionTitle = question.getTitle();
            }
        } catch (SQLException e) {
            questionTitle = "Question #" + answer.getQuestionId();
        }
        
        // Header Row: Question Title
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Q: " + questionTitle);
        title.setWrapText(true);
        title.getStyleClass().add("answer-question-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        // Accepted Badge
        if (answer.isAccepted()) {
            Label acceptedBadge = new Label("✓ Accepted");
            acceptedBadge.getStyleClass().add("accepted-badge");
            headerRow.getChildren().add(acceptedBadge);
        }
        
        headerRow.getChildren().add(0, title);
        
        // Answer Content Preview
        String contentPreview = answer.getContent();
        if (contentPreview.length() > 150) {
            contentPreview = contentPreview.substring(0, 147) + "...";
        }
        Label content = new Label(contentPreview);
        content.setWrapText(true);
        content.getStyleClass().add("answer-content-preview");
        
        // Info Row
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        int netVotes = answer.getUpvotes() - answer.getDownvotes();
        String voteIcon = netVotes > 0 ? "👍" : (netVotes < 0 ? "👎" : "➖");
        Label votes = new Label(voteIcon + " " + netVotes + " votes");
        votes.getStyleClass().add("answer-meta");
        
        Label date = new Label("🕒 " + formatDate(answer.getCreatedAt()));
        date.getStyleClass().add("answer-meta");
        
        HBox earnings = new HBox(5);
        earnings.setAlignment(Pos.CENTER_LEFT);
        if (answer.isAccepted()) {
            Label coinsLabel = new Label("💰 " + answer.getCoinsAwarded() + " coins earned");
            coinsLabel.getStyleClass().add("answer-meta");
            if (answer.getRating() > 0) {
                Label ratingLabel = new Label("⭐ " + answer.getRating() + "/5");
                ratingLabel.getStyleClass().add("answer-meta");
                earnings.getChildren().addAll(coinsLabel, new Label("|"), ratingLabel);
            } else {
                earnings.getChildren().add(coinsLabel);
            }
        } else {
            Label pendingLabel = new Label("⏳ Pending acceptance");
            pendingLabel.getStyleClass().add("answer-meta");
            earnings.getChildren().add(pendingLabel);
        }
        
        infoRow.getChildren().addAll(votes, date, earnings);
        
        // Action Row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewBtn = new Button("View Question");
        viewBtn.getStyleClass().add("secondary-button");
        viewBtn.setOnAction(e -> viewQuestion(answer.getQuestionId()));
        
        actionRow.getChildren().add(viewBtn);
        
        card.getChildren().addAll(headerRow, content, infoRow, actionRow);
        
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
    private void viewQuestion(int questionId) {
        try {
            // Try to find the Dashboard's content area (when loaded inside Dashboard)
            StackPane dashboardContentArea = findDashboardContentArea();
            
            if (dashboardContentArea != null) {
                // Load into Dashboard's content area
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionDetail.fxml"));
                Parent questionView = loader.load();
                
                QuestionDetailController controller = loader.getController();
                controller.loadQuestion(questionId);
                
                dashboardContentArea.getChildren().clear();
                dashboardContentArea.getChildren().add(questionView);
            } else {
                // Fallback to switching scene
                SessionManager.getInstance().setAttribute("viewQuestionId", questionId);
                Main.switchScene("/fxml/QuestionDetail.fxml", "KnA - Question Details");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open question.");
        }
    }
    
    /**
     * Find the Dashboard's content area by traversing up the scene graph.
     */
    private StackPane findDashboardContentArea() {
        try {
            javafx.scene.Node node = answersList;
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
     * Filter: Show all answers.
     */
    @FXML
    private void showAllAnswers() {
        currentFilter = "all";
        updateActiveTab(allBtn);
        displayAnswers(allAnswers);
    }
    
    /**
     * Filter: Show accepted answers.
     */
    @FXML
    private void showAcceptedAnswers() {
        currentFilter = "accepted";
        updateActiveTab(acceptedBtn);
        displayAnswers(allAnswers);
    }
    
    /**
     * Filter: Show top rated answers.
     */
    @FXML
    private void showTopRatedAnswers() {
        currentFilter = "topRated";
        updateActiveTab(topRatedBtn);
        displayAnswers(allAnswers);
    }
    
    /**
     * Update active tab styling.
     */
    private void updateActiveTab(Button activeButton) {
        allBtn.getStyleClass().remove("active-tab");
        acceptedBtn.getStyleClass().remove("active-tab");
        topRatedBtn.getStyleClass().remove("active-tab");
        
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Navigate to browse questions.
     */
    @FXML
    private void browseQuestions() {
        try {
            Main.switchScene("Dashboard.fxml", "KnA - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Dashboard.");
        }
    }
    
    /**
     * Show empty state.
     */
    private void showEmptyState() {
        answersScrollPane.setManaged(false);
        answersScrollPane.setVisible(false);
        emptyStatePane.setManaged(true);
        emptyStatePane.setVisible(true);
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        answersScrollPane.setManaged(true);
        answersScrollPane.setVisible(true);
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
