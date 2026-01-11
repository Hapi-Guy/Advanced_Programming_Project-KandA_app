package com.kna.controller;

import java.sql.SQLException;
import java.util.List;

import com.kna.Main;
import com.kna.dao.NotificationDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.service.QuestionService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * DashboardController - Main dashboard controller
 */
public class DashboardController {
    
    @FXML private TextField searchField;
    @FXML private Label coinLabel;
    @FXML private Label reputationLabel;
    @FXML private Label notificationBadge;
    @FXML private Button profileButton;
    @FXML private Button adminPanelButton;
    @FXML private StackPane contentArea;
    @FXML private ScrollPane homeScrollPane;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private CheckBox urgentOnlyCheckbox;
    @FXML private CheckBox unansweredOnlyCheckbox;
    @FXML private VBox questionFeedContainer;
    
    // Dashboard stat labels
    @FXML private Label totalQuestionsLabel;
    @FXML private Label answeredQuestionsLabel;
    @FXML private Label pendingQuestionsLabel;
    @FXML private Label urgentQuestionsLabel;
    
    private final AuthService authService;
    private final QuestionService questionService;
    private final NotificationDAO notificationDAO;
    private User currentUser;

    public DashboardController() {
        this.authService = new AuthService();
        this.questionService = new QuestionService();
        this.notificationDAO = new NotificationDAO();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            ToastNotification.showError("Please login first");
            Main.switchScene("/fxml/Login.fxml", "KnA - Login");
            return;
        }
        
        // Populate category filter
        categoryFilter.getItems().addAll(
            "All", "CSE", "EEE", "ECE", "MTE", "CE", "ME", "IEM", "TE", "Arch", "URP", 
            "BME", "MSE", "LE", "ESE", "BECM", "ChE", "MATH", "HUM", "PHY", "CHEM", "General"
        );
        
        // Update UI with user info
        updateUserInfo();
        
        // Load question feed
        loadQuestionFeed();
        
        // Set default filter
        categoryFilter.setValue("All");
        
        // Show admin panel button if user is admin
        if (currentUser.isAdmin()) {
            adminPanelButton.setVisible(true);
        }
        
        // Start notification checker
        startNotificationChecker();
    }

    private void updateUserInfo() {
        coinLabel.setText(String.valueOf(currentUser.getCoins()));
        // Admin users don't have reputation points
        reputationLabel.setText(currentUser.isAdmin() ? "0" : String.valueOf(currentUser.getReputation()));
        profileButton.setText(currentUser.getName());
        
        // Update notification badge
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        try {
            int unreadCount = notificationDAO.getUnreadCount(currentUser.getUserId());
            if (unreadCount > 0) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startNotificationChecker() {
        // Check notifications every 30 seconds
        Thread notificationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    Platform.runLater(this::updateNotificationBadge);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    @FXML
    private void loadQuestionFeed() {
        questionFeedContainer.getChildren().clear();
        
        try {
            String category = categoryFilter.getValue();
            boolean urgentOnly = urgentOnlyCheckbox.isSelected();
            boolean unansweredOnly = unansweredOnlyCheckbox.isSelected();

            List<Question> questions = questionService.getQuestions(
                category,
                urgentOnly ? true : null,
                unansweredOnly ? true : null,
                50,
                0
            );
            
            // Update dashboard stats
            updateDashboardStats(questions);

            if (questions.isEmpty()) {
                Label emptyLabel = new Label("No questions found. Be the first to ask!");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
                questionFeedContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Question question : questions) {
                VBox questionCard = createQuestionCard(question);
                questionFeedContainer.getChildren().add(questionCard);
            }

        } catch (Exception e) {
            ToastNotification.showError("Failed to load questions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateDashboardStats(List<Question> questions) {
        if (totalQuestionsLabel != null) {
            int total = questions.size();
            int answered = (int) questions.stream().filter(Question::isAnswered).count();
            int pending = total - answered;
            int urgent = (int) questions.stream().filter(Question::isUrgent).count();
            
            totalQuestionsLabel.setText(String.valueOf(total));
            answeredQuestionsLabel.setText(String.valueOf(answered));
            pendingQuestionsLabel.setText(String.valueOf(pending));
            urgentQuestionsLabel.setText(String.valueOf(urgent));
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.getStyleClass().add("question-card");
        if (question.isUrgent()) {
            card.getStyleClass().add("question-urgent");
        }
        card.setPadding(new Insets(15));
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // Title
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        
        // Description preview
        String description = question.getDescription();
        if (description.length() > 150) {
            description = description.substring(0, 150) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #757575;");
        
        // Meta info
        HBox metaBox = new HBox(15);
        metaBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label categoryBadge = new Label(question.getCategory());
        categoryBadge.getStyleClass().add("badge");
        
        Label coinBadge = new Label("💰 " + question.getCoinReward());
        coinBadge.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-padding: 4px 10px; -fx-background-radius: 10px;");
        
        if (question.isUrgent()) {
            Label urgentBadge = new Label("URGENT");
            urgentBadge.getStyleClass().addAll("badge", "badge-urgent");
            metaBox.getChildren().add(urgentBadge);
        }
        
        if (question.isAnswered()) {
            Label answeredBadge = new Label("✓ ANSWERED");
            answeredBadge.getStyleClass().addAll("badge", "badge-success");
            metaBox.getChildren().add(answeredBadge);
        }
        
        Label askedBy = new Label("Asked by: " + question.getUserName());
        askedBy.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");
        
        metaBox.getChildren().addAll(categoryBadge, coinBadge, askedBy);
        
        card.getChildren().addAll(titleLabel, descLabel, metaBox);
        
        // Click handler to view question details
        card.setOnMouseClicked(event -> viewQuestionDetails(question.getQuestionId()));
        
        return card;
    }

    private void viewQuestionDetails(int questionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionDetail.fxml"));
            Parent questionView = loader.load();
            
            QuestionDetailController controller = loader.getController();
            controller.loadQuestion(questionId);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(questionView);
            
        } catch (Exception e) {
            ToastNotification.showError("Failed to load question: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilters() {
        loadQuestionFeed();
    }

    @FXML
    private void refreshFeed() {
        loadQuestionFeed();
        updateUserInfo();
        ToastNotification.showInfo("Feed refreshed");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            ToastNotification.showWarning("Please enter a search query");
            return;
        }
        
        try {
            // Navigate to search results page
            SessionManager.getInstance().setAttribute("searchQuery", searchTerm);
            Main.switchScene("SearchResults.fxml", "KnA - Search Results");
        } catch (Exception e) {
            ToastNotification.showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Notifications.fxml"));
            Parent notificationView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(notificationView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load notifications");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            Parent profileView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load profile");
            e.printStackTrace();
        }
    }

    @FXML
    private void showHome() {
        // Show the home view without reloading the entire scene
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeScrollPane);
        loadQuestionFeed();
        updateUserInfo();
    }

    @FXML
    private void showAskQuestion() {
        try {
            // Check if user can ask questions
            if (!questionService.canAskQuestion(currentUser.getUserId())) {
                int unevaluated = questionService.getUnevaluatedCount(currentUser.getUserId());
                ToastNotification.showWarning("You have " + unevaluated + " unevaluated questions. Please evaluate them first.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AskQuestion.fxml"));
            Parent askView = loader.load();
            
            // Wrap in ScrollPane for proper scrolling
            ScrollPane scrollPane = new ScrollPane(askView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load ask question form");
            e.printStackTrace();
        }
    }

    @FXML
    private void showMyQuestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MyQuestions.fxml"));
            Parent myQuestionsView = loader.load();
            
            // MyQuestions already has its own ScrollPane, so just add it directly
            contentArea.getChildren().clear();
            contentArea.getChildren().add(myQuestionsView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load your questions");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showMyAnswers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MyAnswers.fxml"));
            Parent myAnswersView = loader.load();
            
            // MyAnswers already has its own ScrollPane, so just add it directly
            contentArea.getChildren().clear();
            contentArea.getChildren().add(myAnswersView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load your answers");
            e.printStackTrace();
        }
    }

    @FXML
    private void showLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Leaderboard.fxml"));
            Parent leaderboardView = loader.load();
            
            // Leaderboard already has its own structure, add directly
            contentArea.getChildren().clear();
            contentArea.getChildren().add(leaderboardView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load leaderboard");
            e.printStackTrace();
        }
    }

    @FXML
    private void showCoinPurchase() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CoinPurchase.fxml"));
            Parent coinPurchaseView = loader.load();
            
            // CoinPurchase already has ScrollPane, add directly
            contentArea.getChildren().clear();
            contentArea.getChildren().add(coinPurchaseView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load coin purchase");
            e.printStackTrace();
        }
    }

    @FXML
    private void showAdminPanel() {
        if (!currentUser.isAdmin()) {
            ToastNotification.showError("Access denied");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminPanel.fxml"));
            Parent adminView = loader.load();
            
            // AdminPanel already has its own structure, add directly
            contentArea.getChildren().clear();
            contentArea.getChildren().add(adminView);
        } catch (Exception e) {
            ToastNotification.showError("Failed to load admin panel");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to access your account.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            authService.logout();
            ToastNotification.showInfo("Logged out successfully");
            Main.switchScene("/fxml/Login.fxml", "KnA - Login");
        }
    }
}
