package com.kna.controller;

import com.kna.Main;
import com.kna.dao.UserDAO;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the Leaderboard page.
 * Displays top users by reputation with weekly/monthly/all-time views.
 */
public class LeaderboardController {
    
    @FXML private Button backButton;
    
    // Tab Buttons
    @FXML private Button allTimeBtn;
    @FXML private Button monthlyBtn;
    @FXML private Button weeklyBtn;
    
    // Podium (Top 3)
    @FXML private HBox podiumBox;
    @FXML private VBox firstPlace;
    @FXML private VBox secondPlace;
    @FXML private VBox thirdPlace;
    @FXML private Label firstAvatar;
    @FXML private Label firstName;
    @FXML private Label firstScore;
    @FXML private Label secondAvatar;
    @FXML private Label secondName;
    @FXML private Label secondScore;
    @FXML private Label thirdAvatar;
    @FXML private Label thirdName;
    @FXML private Label thirdScore;
    
    // Rankings List
    @FXML private ScrollPane leaderboardScrollPane;
    @FXML private VBox leaderboardList;
    @FXML private StackPane emptyStatePane;
    
    private UserDAO userDAO;
    private User currentUser;
    private String currentPeriod = "all";
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            loadLeaderboard();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load and display leaderboard data.
     */
    private void loadLeaderboard() {
        try {
            List<User> topUsers = userDAO.getTopUsersByReputation(50); // Get top 50
            
            if (topUsers.isEmpty()) {
                showEmptyState();
                return;
            }
            
            hideEmptyState();
            displayPodium(topUsers);
            displayRankings(topUsers);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load leaderboard.");
        }
    }
    
    /**
     * Display top 3 users in podium.
     */
    private void displayPodium(List<User> users) {
        // Clear podium
        clearPodium();
        
        // First place
        if (users.size() >= 1) {
            User first = users.get(0);
            firstAvatar.setText(getInitials(first.getName()));
            firstName.setText(first.getName());
            firstScore.setText(String.valueOf(first.getReputation()));
            firstPlace.setVisible(true);
            firstPlace.setManaged(true);
        }
        
        // Second place
        if (users.size() >= 2) {
            User second = users.get(1);
            secondAvatar.setText(getInitials(second.getName()));
            secondName.setText(second.getName());
            secondScore.setText(String.valueOf(second.getReputation()));
            secondPlace.setVisible(true);
            secondPlace.setManaged(true);
        }
        
        // Third place
        if (users.size() >= 3) {
            User third = users.get(2);
            thirdAvatar.setText(getInitials(third.getName()));
            thirdName.setText(third.getName());
            thirdScore.setText(String.valueOf(third.getReputation()));
            thirdPlace.setVisible(true);
            thirdPlace.setManaged(true);
        }
    }
    
    /**
     * Clear podium display.
     */
    private void clearPodium() {
        firstPlace.setVisible(false);
        firstPlace.setManaged(false);
        secondPlace.setVisible(false);
        secondPlace.setManaged(false);
        thirdPlace.setVisible(false);
        thirdPlace.setManaged(false);
    }
    
    /**
     * Display rankings list (4th place onwards).
     */
    private void displayRankings(List<User> users) {
        leaderboardList.getChildren().clear();
        
        // Skip top 3 if showing podium
        int startRank = users.size() >= 3 ? 3 : 0;
        
        for (int i = startRank; i < users.size(); i++) {
            User user = users.get(i);
            int rank = i + 1;
            HBox rankCard = createRankCard(rank, user);
            leaderboardList.getChildren().add(rankCard);
        }
    }
    
    /**
     * Create a ranking card UI element.
     */
    private HBox createRankCard(int rank, User user) {
        HBox card = new HBox(15);
        card.getStyleClass().add("rank-card");
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Rank number
        Label rankLabel = new Label("#" + rank);
        rankLabel.getStyleClass().add("rank-number");
        rankLabel.setMinWidth(50);
        
        // Avatar
        StackPane avatarPane = new StackPane();
        avatarPane.getStyleClass().add("avatar-small");
        Label avatarLabel = new Label(getInitials(user.getName()));
        avatarLabel.getStyleClass().add("avatar-text-small");
        avatarPane.getChildren().add(avatarLabel);
        
        // User info
        VBox userInfo = new VBox(3);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        
        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().add("rank-name");
        
        HBox stats = new HBox(15);
        stats.setAlignment(Pos.CENTER_LEFT);
        
        Label dept = new Label("📚 " + user.getDepartment());
        dept.getStyleClass().add("rank-meta");
        
        Label answers = new Label("💬 " + user.getAnswersGiven() + " answers");
        answers.getStyleClass().add("rank-meta");
        
        Label acceptance = new Label("✓ " + String.format("%.1f%%", user.getAcceptanceRate()) + " accepted");
        acceptance.getStyleClass().add("rank-meta");
        
        stats.getChildren().addAll(dept, answers, acceptance);
        userInfo.getChildren().addAll(nameLabel, stats);
        
        // Reputation score
        VBox scoreBox = new VBox(3);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label scoreLabel = new Label(String.valueOf(user.getReputation()));
        scoreLabel.getStyleClass().add("rank-score");
        
        Label scoreText = new Label("reputation");
        scoreText.getStyleClass().add("rank-score-label");
        
        scoreBox.getChildren().addAll(scoreLabel, scoreText);
        
        // Highlight current user
        if (currentUser != null && user.getId() == currentUser.getId()) {
            card.getStyleClass().add("current-user-rank");
        }
        
        card.getChildren().addAll(rankLabel, avatarPane, userInfo, scoreBox);
        
        return card;
    }
    
    /**
     * Get initials from a full name.
     */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }
    
    /**
     * Show all time leaderboard.
     */
    @FXML
    private void showAllTime() {
        currentPeriod = "all";
        updateActiveTab(allTimeBtn);
        loadLeaderboard();
    }
    
    /**
     * Show monthly leaderboard.
     */
    @FXML
    private void showMonthly() {
        currentPeriod = "monthly";
        updateActiveTab(monthlyBtn);
        showInfo("Monthly leaderboard coming soon!");
        // TODO: Implement monthly filtering based on created_at timestamps
    }
    
    /**
     * Show weekly leaderboard.
     */
    @FXML
    private void showWeekly() {
        currentPeriod = "weekly";
        updateActiveTab(weeklyBtn);
        showInfo("Weekly leaderboard coming soon!");
        // TODO: Implement weekly filtering based on created_at timestamps
    }
    
    /**
     * Update active tab styling.
     */
    private void updateActiveTab(Button activeButton) {
        allTimeBtn.getStyleClass().remove("active-tab");
        monthlyBtn.getStyleClass().remove("active-tab");
        weeklyBtn.getStyleClass().remove("active-tab");
        
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Show empty state.
     */
    private void showEmptyState() {
        podiumBox.setManaged(false);
        podiumBox.setVisible(false);
        leaderboardScrollPane.setManaged(false);
        leaderboardScrollPane.setVisible(false);
        emptyStatePane.setManaged(true);
        emptyStatePane.setVisible(true);
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        podiumBox.setManaged(true);
        podiumBox.setVisible(true);
        leaderboardScrollPane.setManaged(true);
        leaderboardScrollPane.setVisible(true);
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
     * Show info toast notification.
     */
    private void showInfo(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.INFO);
    }
    
    /**
     * Show error toast notification.
     */
    private void showError(String message) {
        ToastNotification.show(backButton.getScene().getWindow(), message, ToastNotification.Type.ERROR);
    }
}
