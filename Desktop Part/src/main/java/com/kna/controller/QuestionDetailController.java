package com.kna.controller;

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
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * QuestionDetailController - Displays question details and answers
 */
public class QuestionDetailController {
    
    @FXML private VBox questionCard;
    @FXML private Label titleLabel;
    @FXML private Label categoryBadge;
    @FXML private Label coinRewardLabel;
    @FXML private Label urgentBadge;
    @FXML private Label answeredBadge;
    @FXML private Label descriptionLabel;
    @FXML private VBox imageContainer;
    @FXML private Label askedByLabel;
    @FXML private Label timeLabel;
    @FXML private Label viewCountLabel;
    @FXML private VBox answerFormContainer;
    @FXML private TextArea answerTextArea;
    @FXML private Label answersHeaderLabel;
    @FXML private VBox answersContainer;
    
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
        titleLabel.setText(currentQuestion.getTitle());
        categoryBadge.setText(currentQuestion.getCategory());
        coinRewardLabel.setText("💰 " + currentQuestion.getCoinReward() + " Coins Reward");
        coinRewardLabel.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-padding: 4px 10px; -fx-background-radius: 10px;");
        descriptionLabel.setText(currentQuestion.getDescription());
        
        if (currentQuestion.isUrgent()) {
            urgentBadge.setText("URGENT");
            urgentBadge.setVisible(true);
        }
        
        if (currentQuestion.isAnswered()) {
            answeredBadge.setText("✓ ANSWERED");
            answeredBadge.setVisible(true);
        }
        
        askedByLabel.setText("Asked by: " + currentQuestion.getUserName());
        
        if (currentQuestion.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            timeLabel.setText("Asked: " + currentQuestion.getCreatedAt().toLocalDateTime().format(formatter));
        }
        
        viewCountLabel.setText("Views: " + currentQuestion.getViewCount());
        
        // Show answer form if not question owner
        if (currentUser.getUserId() != currentQuestion.getUserId()) {
            answerFormContainer.setVisible(true);
        }
    }

    private void loadAnswers() {
        try {
            List<Answer> answers = answerService.getAnswers(currentQuestion.getQuestionId());
            
            answersHeaderLabel.setText("Answers (" + answers.size() + ")");
            answersContainer.getChildren().clear();
            
            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No answers yet. Be the first to answer!");
                noAnswersLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
                answersContainer.getChildren().add(noAnswersLabel);
                return;
            }
            
            for (Answer answer : answers) {
                VBox answerCard = createAnswerCard(answer);
                answersContainer.getChildren().add(answerCard);
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
        
        if (content == null || content.trim().isEmpty()) {
            ToastNotification.showWarning("Please write an answer");
            return;
        }
        
        try {
            answerService.submitAnswer(currentQuestion.getQuestionId(), content);
            ToastNotification.showSuccess("Answer submitted successfully!");
            
            answerTextArea.clear();
            loadAnswers();
            
        } catch (Exception e) {
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
