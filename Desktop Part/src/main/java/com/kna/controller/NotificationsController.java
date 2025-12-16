package com.kna.controller;

import com.kna.Main;
import com.kna.dao.NotificationDAO;
import com.kna.model.Notification;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controller for the Notifications page.
 * Displays user notifications with filtering options.
 */
public class NotificationsController {
    
    @FXML private Button backButton;
    @FXML private ScrollPane notificationsScrollPane;
    @FXML private VBox notificationsList;
    @FXML private StackPane emptyStatePane;
    
    // Filter Tabs
    @FXML private Button allTabBtn;
    @FXML private Button unreadTabBtn;
    @FXML private Button answersTabBtn;
    @FXML private Button coinsTabBtn;
    
    private NotificationDAO notificationDAO;
    private User currentUser;
    private String currentFilter = "all";
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        notificationDAO = new NotificationDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            loadNotifications();
        } else {
            showError("Session expired. Please login again.");
            goToLogin();
        }
    }
    
    /**
     * Load and display notifications based on current filter.
     */
    private void loadNotifications() {
        try {
            List<Notification> notifications;
            
            switch (currentFilter) {
                case "unread":
                    notifications = notificationDAO.getUnreadNotifications(currentUser.getId());
                    break;
                case "answers":
                    notifications = notificationDAO.getNotificationsByType(currentUser.getId(), "new_answer");
                    break;
                case "coins":
                    notifications = filterCoinNotifications(notificationDAO.getNotificationsByUserId(currentUser.getId()));
                    break;
                default:
                    notifications = notificationDAO.getNotificationsByUserId(currentUser.getId());
                    break;
            }
            
            displayNotifications(notifications);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load notifications.");
        }
    }
    
    /**
     * Filter notifications related to coins.
     */
    private List<Notification> filterCoinNotifications(List<Notification> notifications) {
        return notifications.stream()
            .filter(n -> n.getType().contains("coin") || 
                        n.getType().equals("accepted_answer") ||
                        n.getType().equals("low_balance"))
            .toList();
    }
    
    /**
     * Display notifications in the list.
     */
    private void displayNotifications(List<Notification> notifications) {
        notificationsList.getChildren().clear();
        
        if (notifications.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        
        for (Notification notification : notifications) {
            VBox notificationCard = createNotificationCard(notification);
            notificationsList.getChildren().add(notificationCard);
        }
    }
    
    /**
     * Create a notification card UI element.
     */
    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(10);
        card.getStyleClass().add("notification-card");
        if (!notification.isRead()) {
            card.getStyleClass().add("unread-notification");
        }
        card.setPadding(new Insets(15, 20, 15, 20));
        
        // Top row: Icon, Message, Time
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.TOP_LEFT);
        
        // Notification icon
        Label icon = new Label(getNotificationIcon(notification.getType()));
        icon.setStyle("-fx-font-size: 24px;");
        
        // Message area
        VBox messageArea = new VBox(5);
        HBox.setHgrow(messageArea, Priority.ALWAYS);
        
        Label message = new Label(notification.getMessage());
        message.setWrapText(true);
        message.getStyleClass().add("notification-message");
        
        Label time = new Label(formatTimeAgo(notification.getCreatedAt()));
        time.getStyleClass().add("notification-time");
        
        messageArea.getChildren().addAll(message, time);
        
        // Unread indicator
        StackPane unreadIndicator = new StackPane();
        if (!notification.isRead()) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 16px;");
            unreadIndicator.getChildren().add(dot);
        }
        
        topRow.getChildren().addAll(icon, messageArea, unreadIndicator);
        
        // Action buttons
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        if (!notification.isRead()) {
            Button markReadBtn = new Button("Mark as Read");
            markReadBtn.getStyleClass().addAll("link-button");
            markReadBtn.setOnAction(e -> markAsRead(notification));
            actionRow.getChildren().add(markReadBtn);
        }
        
        // Add view button for question/answer notifications
        if (notification.getReferenceId() != null && notification.getReferenceId() > 0) {
            Button viewBtn = new Button("View");
            viewBtn.getStyleClass().addAll("secondary-button", "small-button");
            viewBtn.setOnAction(e -> navigateToReference(notification));
            actionRow.getChildren().add(viewBtn);
        }
        
        card.getChildren().addAll(topRow, actionRow);
        
        // Click to mark as read
        card.setOnMouseClicked(e -> {
            if (!notification.isRead()) {
                markAsRead(notification);
            }
        });
        
        return card;
    }
    
    /**
     * Get emoji icon for notification type.
     */
    private String getNotificationIcon(String type) {
        return switch (type) {
            case "new_answer" -> "💬";
            case "accepted_answer" -> "✅";
            case "coins_earned" -> "💰";
            case "low_balance" -> "⚠️";
            case "upvote" -> "👍";
            case "announcement" -> "📢";
            default -> "🔔";
        };
    }
    
    /**
     * Format timestamp as relative time (e.g., "2 hours ago").
     */
    private String formatTimeAgo(LocalDateTime timestamp) {
        if (timestamp == null) return "Unknown time";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(timestamp, now);
        long hours = ChronoUnit.HOURS.between(timestamp, now);
        long days = ChronoUnit.DAYS.between(timestamp, now);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            return timestamp.format(formatter);
        }
    }
    
    /**
     * Mark a notification as read.
     */
    private void markAsRead(Notification notification) {
        try {
            notificationDAO.markAsRead(notification.getId());
            notification.setRead(true);
            loadNotifications(); // Refresh list
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to mark notification as read.");
        }
    }
    
    /**
     * Mark all notifications as read.
     */
    @FXML
    private void markAllAsRead() {
        try {
            notificationDAO.markAllAsRead(currentUser.getId());
            showSuccess("All notifications marked as read.");
            loadNotifications(); // Refresh list
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to mark all notifications as read.");
        }
    }
    
    /**
     * Navigate to the question/answer referenced in the notification.
     */
    private void navigateToReference(Notification notification) {
        if (notification.getReferenceId() != null && notification.getReferenceId() > 0) {
            try {
                // Store question ID in session for QuestionDetailController
                SessionManager.getInstance().setAttribute("viewQuestionId", notification.getReferenceId());
                Main.switchScene("QuestionDetail.fxml", "KnA - Question Details");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to navigate to question.");
            }
        }
    }
    
    /**
     * Filter: Show all notifications.
     */
    @FXML
    private void showAllNotifications() {
        currentFilter = "all";
        updateActiveTab(allTabBtn);
        loadNotifications();
    }
    
    /**
     * Filter: Show unread notifications only.
     */
    @FXML
    private void showUnreadNotifications() {
        currentFilter = "unread";
        updateActiveTab(unreadTabBtn);
        loadNotifications();
    }
    
    /**
     * Filter: Show answer notifications only.
     */
    @FXML
    private void showAnswerNotifications() {
        currentFilter = "answers";
        updateActiveTab(answersTabBtn);
        loadNotifications();
    }
    
    /**
     * Filter: Show coin-related notifications only.
     */
    @FXML
    private void showCoinNotifications() {
        currentFilter = "coins";
        updateActiveTab(coinsTabBtn);
        loadNotifications();
    }
    
    /**
     * Update the active tab styling.
     */
    private void updateActiveTab(Button activeButton) {
        allTabBtn.getStyleClass().remove("active-tab");
        unreadTabBtn.getStyleClass().remove("active-tab");
        answersTabBtn.getStyleClass().remove("active-tab");
        coinsTabBtn.getStyleClass().remove("active-tab");
        
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Show empty state when no notifications.
     */
    private void showEmptyState() {
        notificationsScrollPane.setManaged(false);
        notificationsScrollPane.setVisible(false);
        emptyStatePane.setManaged(true);
        emptyStatePane.setVisible(true);
    }
    
    /**
     * Hide empty state.
     */
    private void hideEmptyState() {
        notificationsScrollPane.setManaged(true);
        notificationsScrollPane.setVisible(true);
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
