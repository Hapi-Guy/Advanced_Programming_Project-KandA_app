package com.kna.controller;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.kna.Main;
import com.kna.dao.AnswerDAO;
import com.kna.dao.QuestionDAO;
import com.kna.model.Answer;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.AnswerService;
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
 * Controller for My Answers page.
 * Displays all answers submitted by the current user.
 */
public class MyAnswersController {
    
    // Stats
    @FXML private Label totalAnswersLabel;
    @FXML private Label acceptedAnswersLabel;
    @FXML private Label pendingAnswersLabel;
    @FXML private Label totalCoinsEarnedLabel;
    
    // Filter ComboBox
    @FXML private ComboBox<String> filterComboBox;
    
    // Content Areas
    @FXML private VBox answersContainer;
    
    private AnswerDAO answerDAO;
    private QuestionDAO questionDAO;
    private AnswerService answerService;
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
        answerService = new AnswerService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Initialize filter dropdown
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll("All", "Accepted", "Pending");
            filterComboBox.setValue("All");
        }
        
        if (currentUser != null) {
            loadAnswers();
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
            case "Accepted" -> "accepted";
            case "Pending" -> "pending";
            default -> "all";
        };
        
        displayAnswers(allAnswers);
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
        long pending = allAnswers.stream().filter(a -> !a.isAccepted()).count();
        int coinsEarned = allAnswers.stream()
            .filter(Answer::isAccepted)
            .mapToInt(Answer::getCoinsAwarded)
            .sum();
        
        if (totalAnswersLabel != null) totalAnswersLabel.setText(String.valueOf(total));
        if (acceptedAnswersLabel != null) acceptedAnswersLabel.setText(String.valueOf(accepted));
        if (pendingAnswersLabel != null) pendingAnswersLabel.setText(String.valueOf(pending));
        if (totalCoinsEarnedLabel != null) totalCoinsEarnedLabel.setText(String.valueOf(coinsEarned));
    }
    
    /**
     * Display answers based on current filter.
     */
    private void displayAnswers(List<Answer> answers) {
        answersContainer.getChildren().clear();
        
        // Apply filter
        List<Answer> filteredAnswers = switch (currentFilter) {
            case "accepted" -> answers.stream().filter(Answer::isAccepted).toList();
            case "pending" -> answers.stream().filter(a -> !a.isAccepted()).toList();
            default -> answers;
        };
        
        if (filteredAnswers.isEmpty()) {
            // Show empty message
            Label emptyLabel = new Label("No answers found");
            emptyLabel.getStyleClass().add("empty-message");
            answersContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Answer answer : filteredAnswers) {
            VBox answerCard = createAnswerCard(answer);
            answersContainer.getChildren().add(answerCard);
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
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteAnswer(answer));
        
        actionRow.getChildren().addAll(viewBtn, deleteBtn);
        
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
     * Delete an answer with confirmation dialog.
     */
    private void deleteAnswer(Answer answer) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Answer");
        confirmDialog.setHeaderText("Are you sure you want to delete this answer?");
        
        String contentPreview = answer.getContent();
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 97) + "...";
        }
        confirmDialog.setContentText("\"" + contentPreview + "\"\n\n" +
            "This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = answerService.deleteAnswer(answer.getAnswerId());
                if (deleted) {
                    ToastNotification.show("Answer deleted successfully", ToastNotification.NotificationType.SUCCESS);
                    // Reload answers
                    loadAnswers();
                    loadStats();
                } else {
                    showError("Failed to delete answer.");
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
            javafx.scene.Node node = answersContainer;
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
