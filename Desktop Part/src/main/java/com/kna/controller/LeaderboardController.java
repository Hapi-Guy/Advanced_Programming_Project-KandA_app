package com.kna.controller;

import java.sql.SQLException;
import java.util.List;

import com.kna.Main;
import com.kna.dao.UserDAO;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the Leaderboard page.
 * Displays top users by reputation with weekly/monthly/all-time views.
 */
public class LeaderboardController {
    
    // Podium (Top 3) - Cards
    @FXML private VBox firstPlaceCard;
    @FXML private VBox secondPlaceCard;
    @FXML private VBox thirdPlaceCard;
    
    // Podium Labels
    @FXML private Label firstPlaceAvatar;
    @FXML private Label firstPlaceName;
    @FXML private Label firstPlaceScore;
    @FXML private Label secondPlaceAvatar;
    @FXML private Label secondPlaceName;
    @FXML private Label secondPlaceScore;
    @FXML private Label thirdPlaceAvatar;
    @FXML private Label thirdPlaceName;
    @FXML private Label thirdPlaceScore;
    
    // Rankings List
    @FXML private VBox leaderboardList;
    
    private UserDAO userDAO;
    private User currentUser;
    
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
                // Show empty message in list
                Label emptyLabel = new Label("No users found");
                emptyLabel.getStyleClass().add("empty-message");
                leaderboardList.getChildren().add(emptyLabel);
                return;
            }
            
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
        // First place
        if (users.size() >= 1) {
            User first = users.get(0);
            if (firstPlaceAvatar != null) firstPlaceAvatar.setText(getInitials(first.getName()));
            if (firstPlaceName != null) firstPlaceName.setText(first.getName());
            if (firstPlaceScore != null) firstPlaceScore.setText(String.valueOf(first.getReputation()));
            if (firstPlaceCard != null) {
                firstPlaceCard.setVisible(true);
                firstPlaceCard.setManaged(true);
            }
        }
        
        // Second place
        if (users.size() >= 2) {
            User second = users.get(1);
            if (secondPlaceAvatar != null) secondPlaceAvatar.setText(getInitials(second.getName()));
            if (secondPlaceName != null) secondPlaceName.setText(second.getName());
            if (secondPlaceScore != null) secondPlaceScore.setText(String.valueOf(second.getReputation()));
            if (secondPlaceCard != null) {
                secondPlaceCard.setVisible(true);
                secondPlaceCard.setManaged(true);
            }
        }
        
        // Third place
        if (users.size() >= 3) {
            User third = users.get(2);
            if (thirdPlaceAvatar != null) thirdPlaceAvatar.setText(getInitials(third.getName()));
            if (thirdPlaceName != null) thirdPlaceName.setText(third.getName());
            if (thirdPlaceScore != null) thirdPlaceScore.setText(String.valueOf(third.getReputation()));
            if (thirdPlaceCard != null) {
                thirdPlaceCard.setVisible(true);
                thirdPlaceCard.setManaged(true);
            }
        }
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
