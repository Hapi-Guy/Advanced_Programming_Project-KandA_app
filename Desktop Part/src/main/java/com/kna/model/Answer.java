package com.kna.model;

import java.sql.Timestamp;

/**
 * Answer model representing an answer to a question
 */
public class Answer {
    private int answerId;
    private int questionId;
    private int userId;
    private String userName;
    private String content;
    private boolean isAccepted;
    private int rating;
    private int upvotes;
    private int downvotes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Answer() {}

    public Answer(int questionId, int userId, String content) {
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
    }

    // Getters and Setters
    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
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

    public int getNetVotes() {
        return upvotes - downvotes;
    }

    // Coins awarded for this answer (to be calculated separately)
    public int getCoinsAwarded() {
        return 0; // This should be populated from reward calculation
    }

    @Override
    public String toString() {
        return "Answer{" +
                "answerId=" + answerId +
                ", questionId=" + questionId +
                ", isAccepted=" + isAccepted +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                '}';
    }
}
