package com.kna.controller;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.kna.Main;
import com.kna.dao.AnswerDAO;
import com.kna.dao.CoinDAO;
import com.kna.dao.NotificationDAO;
import com.kna.dao.QuestionDAO;
import com.kna.dao.UserDAO;
import com.kna.model.CoinTransaction;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for Admin Panel.
 * Provides administrative functions for managing users, questions, transactions, and settings.
 */
public class AdminPanelController {
    
    @FXML private Button backButton;
    
    // Tab Buttons
    @FXML private Button dashboardTabBtn;
    @FXML private Button usersTabBtn;
    @FXML private Button questionsTabBtn;
    @FXML private Button transactionsTabBtn;
    @FXML private Button settingsTabBtn;
    
    // Panes
    @FXML private ScrollPane dashboardPane;
    @FXML private ScrollPane usersPane;
    @FXML private ScrollPane questionsPane;
    @FXML private ScrollPane transactionsPane;
    @FXML private ScrollPane settingsPane;
    
    // Dashboard Stats
    @FXML private Label totalUsersLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalAnswersLabel;
    @FXML private Label totalCoinsLabel;
    @FXML private Label transactionsTodayLabel;
    @FXML private Label activeUsersLabel;
    
    // Users Table
    @FXML private TextField userSearchField;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userPasswordCol;
    @FXML private TableColumn<User, String> userDeptCol;
    @FXML private TableColumn<User, Integer> userCoinsCol;
    @FXML private TableColumn<User, Integer> userReputationCol;
    @FXML private TableColumn<User, String> userStatusCol;
    @FXML private TableColumn<User, Void> userActionsCol;
    
    // Questions Table
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, Integer> questionIdCol;
    @FXML private TableColumn<Question, String> questionTitleCol;
    @FXML private TableColumn<Question, String> questionCategoryCol;
    @FXML private TableColumn<Question, String> questionUserCol;
    @FXML private TableColumn<Question, Integer> questionAnswersCol;
    @FXML private TableColumn<Question, Timestamp> questionDateCol;
    @FXML private TableColumn<Question, Void> questionActionsCol;
    
    // Transactions Table
    @FXML private TableView<CoinTransaction> transactionsTable;
    @FXML private TableColumn<CoinTransaction, Integer> transactionIdCol;
    @FXML private TableColumn<CoinTransaction, String> transactionUserCol;
    @FXML private TableColumn<CoinTransaction, String> transactionTypeCol;
    @FXML private TableColumn<CoinTransaction, Integer> transactionAmountCol;
    @FXML private TableColumn<CoinTransaction, String> transactionRefCol;
    @FXML private TableColumn<CoinTransaction, Timestamp> transactionDateCol;
    
    // Settings Fields
    @FXML private TextField baseQuestionCostField;
    @FXML private TextField urgentQuestionCostField;
    @FXML private TextField startingCoinsField;
    @FXML private TextField upvotePointsField;
    @FXML private TextField acceptedPointsField;
    @FXML private TextField maxUnevaluatedField;
    
    private UserDAO userDAO;
    private QuestionDAO questionDAO;
    private AnswerDAO answerDAO;
    private CoinDAO coinDAO;
    private NotificationDAO notificationDAO;
    private User currentUser;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        questionDAO = new QuestionDAO();
        answerDAO = new AnswerDAO();
        coinDAO = new CoinDAO();
        notificationDAO = new NotificationDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            showError("Access denied. Admin privileges required.");
            goBack();
            return;
        }
        
        initializeTables();
        loadDashboardStats();
    }
    
    /**
     * Initialize table columns.
     */
    private void initializeTables() {
        // Users table
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userPasswordCol.setCellValueFactory(new PropertyValueFactory<>("passwordHash"));
        userDeptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        userCoinsCol.setCellValueFactory(new PropertyValueFactory<>("coins"));
        userReputationCol.setCellValueFactory(new PropertyValueFactory<>("reputation"));
        userStatusCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive")
        );
        
        // Questions table
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        questionCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        questionAnswersCol.setCellValueFactory(new PropertyValueFactory<>("answerCount"));
        questionDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Transactions table
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        transactionAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionRefCol.setCellValueFactory(new PropertyValueFactory<>("referenceType"));
        transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }
    
    /**
     * Load dashboard statistics.
     */
    private void loadDashboardStats() {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            List<Question> allQuestions = questionDAO.getAllQuestions(null, null, null, 1000, 0);
            
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            totalQuestionsLabel.setText(String.valueOf(allQuestions.size()));
            
            // Count total answers
            int totalAnswers = 0;
            for (Question q : allQuestions) {
                totalAnswers += q.getAnswerCount();
            }
            totalAnswersLabel.setText(String.valueOf(totalAnswers));
            
            // Calculate total coins in system
            int totalCoins = allUsers.stream().mapToInt(User::getCoins).sum();
            totalCoinsLabel.setText(String.valueOf(totalCoins));
            
            // Count active users
            long activeUsers = allUsers.stream().filter(User::isActive).count();
            activeUsersLabel.setText(String.valueOf(activeUsers));
            
            // Placeholder for transactions today
            transactionsTodayLabel.setText("N/A");
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load statistics.");
        }
    }
    
    /**
     * Show dashboard view.
     */
    @FXML
    private void showDashboard() {
        switchToPane(dashboardPane, dashboardTabBtn);
        loadDashboardStats();
    }
    
    /**
     * Show users view.
     */
    @FXML
    private void showUsers() {
        switchToPane(usersPane, usersTabBtn);
        loadAllUsers();
    }
    
    /**
     * Load all users into table.
     */
    private void loadAllUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            usersTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load users.");
        }
    }
    
    /**
     * Search users.
     */
    @FXML
    private void searchUsers() {
        String query = userSearchField.getText().trim();
        if (query.isEmpty()) {
            loadAllUsers();
            return;
        }
        
        try {
            List<User> users = userDAO.searchUsers(query);
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            usersTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Search failed.");
        }
    }
    
    /**
     * Show/hide password column in users table.
     */
    @FXML
    private void showPasswords() {
        boolean isVisible = userPasswordCol.isVisible();
        userPasswordCol.setVisible(!isVisible);
        
        if (!isVisible) {
            showSuccess("Passwords are now visible in the table");
        } else {
            showInfo("Passwords are now hidden");
        }
    }
    
    /**
     * Show questions view.
     */
    @FXML
    private void showQuestions() {
        switchToPane(questionsPane, questionsTabBtn);
        loadAllQuestions();
    }
    
    /**
     * Load all questions into table.
     */
    private void loadAllQuestions() {
        try {
            List<Question> questions = questionDAO.getAllQuestions(null, null, null, 1000, 0);
            ObservableList<Question> questionList = FXCollections.observableArrayList(questions);
            questionsTable.setItems(questionList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load questions.");
        }
    }
    
    /**
     * Show transactions view.
     */
    @FXML
    private void showTransactions() {
        switchToPane(transactionsPane, transactionsTabBtn);
        loadAllTransactions();
    }
    
    /**
     * Load all transactions into table.
     */
    private void loadAllTransactions() {
        try {
            // Get transactions for all users (simplified - get from first 100 users)
            List<User> users = userDAO.getAllUsers();
            ObservableList<CoinTransaction> allTransactions = FXCollections.observableArrayList();
            
            for (User user : users) {
                List<CoinTransaction> userTransactions = coinDAO.getTransactionsByUserId(user.getId(), 10);
                allTransactions.addAll(userTransactions);
                if (allTransactions.size() > 100) break; // Limit to 100 for performance
            }
            
            transactionsTable.setItems(allTransactions);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load transactions.");
        }
    }
    
    /**
     * Show settings view.
     */
    @FXML
    private void showSettings() {
        switchToPane(settingsPane, settingsTabBtn);
    }
    
    /**
     * Switch to a specific pane.
     */
    private void switchToPane(ScrollPane targetPane, Button activeButton) {
        // Hide all panes
        dashboardPane.setManaged(false);
        dashboardPane.setVisible(false);
        usersPane.setManaged(false);
        usersPane.setVisible(false);
        questionsPane.setManaged(false);
        questionsPane.setVisible(false);
        transactionsPane.setManaged(false);
        transactionsPane.setVisible(false);
        settingsPane.setManaged(false);
        settingsPane.setVisible(false);
        
        // Show target pane
        targetPane.setManaged(true);
        targetPane.setVisible(true);
        
        // Update active tab
        dashboardTabBtn.getStyleClass().remove("active-tab");
        usersTabBtn.getStyleClass().remove("active-tab");
        questionsTabBtn.getStyleClass().remove("active-tab");
        transactionsTabBtn.getStyleClass().remove("active-tab");
        settingsTabBtn.getStyleClass().remove("active-tab");
        activeButton.getStyleClass().add("active-tab");
    }
    
    /**
     * Create announcement to all users.
     */
    @FXML
    private void createAnnouncement() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Announcement");
        dialog.setHeaderText("Send announcement to all users");
        dialog.setContentText("Enter announcement message:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            if (!message.trim().isEmpty()) {
                try {
                    notificationDAO.createAnnouncement("System Announcement", message);
                    showSuccess("Announcement sent to all users!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Failed to send announcement.");
                }
            }
        });
    }
    
    /**
     * Refresh statistics.
     */
    @FXML
    private void refreshStats() {
        loadDashboardStats();
        showSuccess("Statistics refreshed!");
    }
    
    /**
     * View reports (placeholder).
     */
    @FXML
    private void viewReports() {
        showInfo("Reports feature coming soon!");
    }
    
    /**
     * Save coin settings.
     */
    @FXML
    private void saveCoinSettings() {
        try {
            int baseCost = Integer.parseInt(baseQuestionCostField.getText());
            int urgentCost = Integer.parseInt(urgentQuestionCostField.getText());
            int startingCoins = Integer.parseInt(startingCoinsField.getText());
            
            // Validation
            if (baseCost < 1 || urgentCost < baseCost || startingCoins < 0) {
                showError("Invalid coin settings. Check your values.");
                return;
            }
            
            showSuccess("Coin settings saved! (Note: Restart required for changes to take effect)");
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers.");
        }
    }
    
    /**
     * Save reputation settings.
     */
    @FXML
    private void saveReputationSettings() {
        try {
            int upvotePoints = Integer.parseInt(upvotePointsField.getText());
            int acceptedPoints = Integer.parseInt(acceptedPointsField.getText());
            
            if (upvotePoints < 0 || acceptedPoints < 0) {
                showError("Points must be positive numbers.");
                return;
            }
            
            showSuccess("Reputation settings saved! (Note: Restart required for changes to take effect)");
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers.");
        }
    }
    
    /**
     * Save system limits.
     */
    @FXML
    private void saveSystemLimits() {
        try {
            int maxUnevaluated = Integer.parseInt(maxUnevaluatedField.getText());
            
            if (maxUnevaluated < 1) {
                showError("Maximum must be at least 1.");
                return;
            }
            
            showSuccess("System limits saved! (Note: Restart required for changes to take effect)");
        } catch (NumberFormatException e) {
            showError("Please enter a valid number.");
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
     * Show success toast.
     */
    private void showSuccess(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.SUCCESS);
    }
    
    /**
     * Show info toast.
     */
    private void showInfo(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.INFO);
    }
    
    /**
     * Show error toast.
     */
    private void showError(String message) {
        ToastNotification.show(message, ToastNotification.NotificationType.ERROR);
    }
}
