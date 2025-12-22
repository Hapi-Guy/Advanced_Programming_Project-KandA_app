package com.kna.model;

import java.sql.Timestamp;

/**
 * Question model representing a question in the KnA application
 */
public class Question {
    private int questionId;
    private int userId;
    private String userName;
    private String title;
    private String description;
    private String category;
    private boolean isUrgent;
    private int coinReward;
    private boolean isAnswered;
    private boolean isEvaluated;
    private Integer acceptedAnswerId;
    private int viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String imagePath;

    // Constructors
    public Question() {}

    public Question(int userId, String title, String description, String category, boolean isUrgent, int coinReward) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isUrgent = isUrgent;
        this.coinReward = coinReward;
    }

    // Getters and Setters
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public void setCoinReward(int coinReward) {
        this.coinReward = coinReward;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }

    public Integer getAcceptedAnswerId() {
        return acceptedAnswerId;
    }

    public void setAcceptedAnswerId(Integer acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Alias for getQuestionId
    public int getId() {
        return questionId;
    }

    public void setId(int id) {
        this.questionId = id;
    }

    // Placeholder for answer count (to be calculated separately)
    public int getAnswerCount() {
        return 0; // This should be populated from DAO
    }

    // Alias for getViewCount
    public int getViews() {
        return viewCount;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", isUrgent=" + isUrgent +
                ", coinReward=" + coinReward +
                '}';
    }
}
