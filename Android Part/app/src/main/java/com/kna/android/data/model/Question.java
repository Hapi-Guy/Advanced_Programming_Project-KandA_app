package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Question entity mapping to 'questions' table
 * Migrated from desktop JavaFX Question.java model
 */
@Entity(
    tableName = "questions",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id"), @Index("category"), @Index("is_urgent")}
)
public class Question {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "question_id")
    private long questionId;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "category")
    private String category;
    
    @ColumnInfo(name = "is_urgent")
    private boolean isUrgent;
    
    @ColumnInfo(name = "coin_reward")
    private int coinReward;
    
    @ColumnInfo(name = "is_answered")
    private boolean isAnswered;
    
    @ColumnInfo(name = "accepted_answer_id")
    private Long acceptedAnswerId; // Nullable
    
    @ColumnInfo(name = "answer_count")
    private int answerCount;
    
    @ColumnInfo(name = "view_count")
    private int viewCount;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructor
    public Question() {
        this.isAnswered = false;
        this.answerCount = 0;
        this.viewCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public Long getAcceptedAnswerId() {
        return acceptedAnswerId;
    }

    public void setAcceptedAnswerId(Long acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
