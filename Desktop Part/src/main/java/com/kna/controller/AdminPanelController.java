package com.kna.controller;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.kna.Main;
import com.kna.dao.QuestionDAO;
import com.kna.dao.UserDAO;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.geometry.Pos;

/**
 * Controller for Admin Panel.
 * Provides administrative functions for managing users, questions, transactions, and settings.
 */
public class AdminPanelController {
    
    // Users Tab Stats
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label activeUsersLabel;
    
    // Questions Tab Stats
    @FXML private Label totalQuestionsLabel;
    @FXML private Label answeredQuestionsLabel;
    @FXML private Label urgentQuestionsLabel;
    
    // Reports Tab Stats
    @FXML private Label totalReportsLabel;
    @FXML private Label pendingReportsLabel;
    @FXML private Label resolvedReportsLabel;
    
    // Users Table
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userUsernameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, Integer> userCoinsCol;
    @FXML private TableColumn<User, Integer> userReputationCol;
    @FXML private TableColumn<User, String> userRoleCol;
    @FXML private TableColumn<User, Void> userActionsCol;
    
    // Questions Table
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, Integer> questionIdCol;
    @FXML private TableColumn<Question, String> questionTitleCol;
    @FXML private TableColumn<Question, String> questionCategoryCol;
    @FXML private TableColumn<Question, Integer> questionRewardCol;
    @FXML private TableColumn<Question, Boolean> questionUrgentCol;
    @FXML private TableColumn<Question, String> questionAskerCol;
    @FXML private TableColumn<Question, Void> questionActionsCol;
    
    // Reports Table
    @FXML private TableView<?> reportsTable;
    @FXML private TableColumn<?, Integer> reportIdCol;
    @FXML private TableColumn<?, String> reportTypeCol;
    @FXML private TableColumn<?, String> reportReasonCol;
    @FXML private TableColumn<?, String> reportStatusCol;
    @FXML private TableColumn<?, Timestamp> reportDateCol;
    @FXML private TableColumn<?, Void> reportActionsCol;
    
    private UserDAO userDAO;
    private QuestionDAO questionDAO;
    private User currentUser;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        questionDAO = new QuestionDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            showError("Access denied. Admin privileges required.");
            goBack();
            return;
        }
        
        initializeTables();
        loadStats();
        loadAllUsers();
        loadAllQuestions();
    }
    
    /**
     * Initialize table columns.
     */
    private void initializeTables() {
        // Users table
        if (userIdCol != null) userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (userUsernameCol != null) userUsernameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (userEmailCol != null) userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userCoinsCol != null) userCoinsCol.setCellValueFactory(new PropertyValueFactory<>("coins"));
        if (userReputationCol != null) userReputationCol.setCellValueFactory(new PropertyValueFactory<>("reputation"));
        if (userRoleCol != null) {
            userRoleCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isAdmin() ? "Admin" : "User")
            );
        }
        
        // Add Reset Password button to actions column
        if (userActionsCol != null) {
            Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
                @Override
                public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                    final TableCell<User, Void> cell = new TableCell<>() {
                        private final Button resetBtn = new Button("Reset Password");
                        {
                            resetBtn.getStyleClass().add("warning-button");
                            resetBtn.setOnAction(event -> {
                                User user = getTableView().getItems().get(getIndex());
                                resetPassword(user);
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(resetBtn);
                                setAlignment(Pos.CENTER);
                            }
                        }
                    };
                    return cell;
                }
            };
            userActionsCol.setCellFactory(cellFactory);
        }
        
        // Questions table
        if (questionIdCol != null) questionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (questionTitleCol != null) questionTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (questionCategoryCol != null) questionCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (questionRewardCol != null) questionRewardCol.setCellValueFactory(new PropertyValueFactory<>("coinReward"));
        if (questionUrgentCol != null) questionUrgentCol.setCellValueFactory(new PropertyValueFactory<>("urgent"));
        if (questionAskerCol != null) questionAskerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
    }
    
    /**
     * Load all statistics.
     */
    private void loadStats() {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            List<Question> allQuestions = questionDAO.getAllQuestions(null, null, null, 1000, 0);
            
            // Users stats
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(allUsers.size()));
            if (totalAdminsLabel != null) {
                long adminCount = allUsers.stream().filter(User::isAdmin).count();
                totalAdminsLabel.setText(String.valueOf(adminCount));
            }
            if (activeUsersLabel != null) {
                long activeUsers = allUsers.stream().filter(User::isActive).count();
                activeUsersLabel.setText(String.valueOf(activeUsers));
            }
            
            // Questions stats
            if (totalQuestionsLabel != null) totalQuestionsLabel.setText(String.valueOf(allQuestions.size()));
            if (answeredQuestionsLabel != null) {
                long answeredCount = allQuestions.stream().filter(Question::isAnswered).count();
                answeredQuestionsLabel.setText(String.valueOf(answeredCount));
            }
            if (urgentQuestionsLabel != null) {
                long urgentCount = allQuestions.stream().filter(Question::isUrgent).count();
                urgentQuestionsLabel.setText(String.valueOf(urgentCount));
            }
            
            // Reports stats (placeholder - set to 0)
            if (totalReportsLabel != null) totalReportsLabel.setText("0");
            if (pendingReportsLabel != null) pendingReportsLabel.setText("0");
            if (resolvedReportsLabel != null) resolvedReportsLabel.setText("0");
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load statistics.");
        }
    }
    
    /**
     * Refresh users data.
     */
    @FXML
    private void refreshUsers() {
        loadAllUsers();
        loadStats();
        showSuccess("Users refreshed!");
    }
    
    /**
     * Load all users into table.
     */
    private void loadAllUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            if (usersTable != null) usersTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load users.");
        }
    }
    
    /**
     * Reset user password to "test123".
     */
    private void resetPassword(User user) {
        if (user == null) return;
        
        try {
            // Set password to default
            userDAO.updatePassword(user.getId(), "test123");
            showSuccess("Password reset for user: " + user.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to reset password for user: " + user.getName());
        }
    }
    
    /**
     * Refresh questions data.
     */
    @FXML
    private void refreshQuestions() {
        loadAllQuestions();
        loadStats();
        showSuccess("Questions refreshed!");
    }
    
    /**
     * Load all questions into table.
     */
    private void loadAllQuestions() {
        try {
            List<Question> questions = questionDAO.getAllQuestions(null, null, null, 1000, 0);
            ObservableList<Question> questionList = FXCollections.observableArrayList(questions);
            if (questionsTable != null) questionsTable.setItems(questionList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load questions.");
        }
    }
    
    /**
     * Refresh reports data.
     */
    @FXML
    private void refreshReports() {
        showInfo("Reports feature coming soon!");
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
