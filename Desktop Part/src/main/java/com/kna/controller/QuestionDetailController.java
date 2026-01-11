package com.kna.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.kna.Main;
import com.kna.model.Answer;
import com.kna.model.Question;
import com.kna.model.User;
import com.kna.service.AnswerService;
import com.kna.service.QuestionService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * QuestionDetailController - Displays question details and answers
 */
public class QuestionDetailController {
    
    // Question Card
    @FXML private Label questionTitleLabel;
    @FXML private Label questionDescriptionLabel;
    @FXML private Label categoryBadge;
    @FXML private Label rewardLabel;
    @FXML private Label urgentBadge;
    @FXML private Label askerLabel;
    @FXML private Label dateLabel;
    @FXML private Label answerCountLabel;
    
    // Answer Form
    @FXML private VBox answerFormContainer;
    @FXML private TextArea answerTextArea;
    @FXML private Label answerErrorLabel;
    
    // Answers Section
    @FXML private Label answersCountBadge;
    @FXML private VBox answersContainer;
    @FXML private VBox noAnswersLabel;
    
    private final QuestionService questionService;
    private final AnswerService answerService;
    private Question currentQuestion;
    private User currentUser;

    public QuestionDetailController() {
        this.questionService = new QuestionService();
        this.answerService = new AnswerService();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Try to load question from session
        Integer questionId = (Integer) SessionManager.getInstance().getAttribute("viewQuestionId");
        if (questionId != null) {
            loadQuestion(questionId);
            SessionManager.getInstance().removeAttribute("viewQuestionId");
        }
    }

    public void loadQuestion(int questionId) {
        try {
            currentQuestion = questionService.getQuestion(questionId);
            
            if (currentQuestion == null) {
                ToastNotification.showError("Question not found");
                return;
            }
            
            displayQuestion();
            loadAnswers();
            
        } catch (Exception e) {
            ToastNotification.showError("Failed to load question: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayQuestion() {
        if (questionTitleLabel != null) questionTitleLabel.setText(currentQuestion.getTitle());
        if (categoryBadge != null) categoryBadge.setText(currentQuestion.getCategory());
        if (rewardLabel != null) rewardLabel.setText(String.valueOf(currentQuestion.getCoinReward()));
        if (questionDescriptionLabel != null) questionDescriptionLabel.setText(currentQuestion.getDescription());
        
        if (currentQuestion.isUrgent()) {
            if (urgentBadge != null) {
                urgentBadge.setVisible(true);
                urgentBadge.setManaged(true);
            }
        }
        
        if (askerLabel != null) askerLabel.setText(currentQuestion.getUserName());
        
        if (currentQuestion.getCreatedAt() != null && dateLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            dateLabel.setText(currentQuestion.getCreatedAt().toLocalDateTime().format(formatter));
        }
        
        if (answerCountLabel != null) {
            answerCountLabel.setText(currentQuestion.getAnswerCount() + " answers");
        }
        
        // Show answer form if not question owner
        if (answerFormContainer != null && currentUser != null) {
            boolean canAnswer = currentUser.getUserId() != currentQuestion.getUserId();
            answerFormContainer.setVisible(canAnswer);
            answerFormContainer.setManaged(canAnswer);
        }
    }

    private void loadAnswers() {
        try {
            List<Answer> answers = answerService.getAnswers(currentQuestion.getQuestionId());
            
            if (answersCountBadge != null) answersCountBadge.setText(String.valueOf(answers.size()));
            if (answersContainer != null) answersContainer.getChildren().clear();
            
            if (answers.isEmpty()) {
                if (noAnswersLabel != null) {
                    noAnswersLabel.setVisible(true);
                    noAnswersLabel.setManaged(true);
                }
                return;
            }
            
            // Hide empty state
            if (noAnswersLabel != null) {
                noAnswersLabel.setVisible(false);
                noAnswersLabel.setManaged(false);
            }
            
            for (Answer answer : answers) {
                VBox answerCard = createAnswerCard(answer);
                if (answersContainer != null) answersContainer.getChildren().add(answerCard);
            }
            
        } catch (Exception e) {
            ToastNotification.showError("Failed to load answers");
            e.printStackTrace();
        }
    }

    private VBox createAnswerCard(Answer answer) {
        VBox card = new VBox(10);
        card.getStyleClass().add("answer-card");
        if (answer.isAccepted()) {
            card.getStyleClass().add("answer-accepted");
        }
        card.setPadding(new Insets(15));
        
        // Answer header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label(answer.getUserName());
        authorLabel.setStyle("-fx-font-weight: bold;");
        
        if (answer.isAccepted()) {
            Label acceptedBadge = new Label("✓ ACCEPTED");
            acceptedBadge.getStyleClass().addAll("badge", "badge-success");
            header.getChildren().add(acceptedBadge);
        }
        
        Label ratingLabel = new Label("Rating: " + answer.getRating() + "/5");
        ratingLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");
        
        header.getChildren().addAll(authorLabel, new Region(), ratingLabel);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        
        // Answer content
        Label contentLabel = new Label(answer.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        // Voting and actions
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Upvote/Downvote
        if (currentUser.getUserId() != answer.getUserId()) {
            Button upvoteButton = new Button("👍 " + answer.getUpvotes());
            upvoteButton.setOnAction(e -> handleVote(answer.getAnswerId(), "upvote"));
            
            Button downvoteButton = new Button("👎 " + answer.getDownvotes());
            downvoteButton.setOnAction(e -> handleVote(answer.getAnswerId(), "downvote"));
            
            actionsBox.getChildren().addAll(upvoteButton, downvoteButton);
        }
        
        // Accept button (for question owner)
        if (currentUser.getUserId() == currentQuestion.getUserId() && !answer.isAccepted() && !currentQuestion.isAnswered()) {
            Button acceptButton = new Button("Accept Answer");
            acceptButton.getStyleClass().add("button-success");
            acceptButton.setOnAction(e -> handleAcceptAnswer(answer.getAnswerId()));
            actionsBox.getChildren().add(acceptButton);
        }
        
        card.getChildren().addAll(header, contentLabel, actionsBox);
        
        return card;
    }

    @FXML
    private void submitAnswer() {
        String content = answerTextArea.getText();
        
        // Clear previous error
        if (answerErrorLabel != null) answerErrorLabel.setText("");
        
        if (content == null || content.trim().isEmpty()) {
            if (answerErrorLabel != null) {
                answerErrorLabel.setText("Please write an answer");
            }
            ToastNotification.showWarning("Please write an answer");
            return;
        }
        
        try {
            answerService.submitAnswer(currentQuestion.getQuestionId(), content);
            ToastNotification.showSuccess("Answer submitted successfully!");
            
            answerTextArea.clear();
            loadAnswers();
            
        } catch (Exception e) {
            if (answerErrorLabel != null) {
                answerErrorLabel.setText(e.getMessage());
            }
            ToastNotification.showError(e.getMessage());
        }
    }

    private void handleVote(int answerId, String voteType) {
        try {
            answerService.voteAnswer(answerId, voteType);
            ToastNotification.showSuccess("Vote recorded!");
            loadAnswers();
        } catch (Exception e) {
            ToastNotification.showError(e.getMessage());
        }
    }

    private void handleAcceptAnswer(int answerId) {
        // Show rating dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Accept Answer");
        dialog.setHeaderText("Rate this answer (0-5 stars)");
        
        ButtonType acceptButtonType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(acceptButtonType, ButtonType.CANCEL);
        
        Slider ratingSlider = new Slider(0, 5, 5);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);
        
        Label ratingLabel = new Label("Rating: 5");
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingLabel.setText("Rating: " + newVal.intValue());
        });
        
        VBox content = new VBox(10, new Label("Slide to rate the answer:"), ratingSlider, ratingLabel);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == acceptButtonType) {
                return (int) ratingSlider.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(rating -> {
            try {
                answerService.acceptAnswer(answerId, rating);
                ToastNotification.showSuccess("Answer accepted!");
                loadQuestion(currentQuestion.getQuestionId());
            } catch (Exception e) {
                ToastNotification.showError(e.getMessage());
            }
        });
    }

    @FXML
    private void goBack() {
        Main.switchScene("/fxml/Dashboard.fxml", "KnA - Dashboard");
    }
}
