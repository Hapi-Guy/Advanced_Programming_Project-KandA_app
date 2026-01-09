package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Answer entity mapping to 'answers' table
 * Migrated from desktop JavaFX Answer.java model
 */
@Entity(
    tableName = "answers",
    foreignKeys = {
        @ForeignKey(
            entity = Question.class,
            parentColumns = "question_id",
            childColumns = "question_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "user_id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("question_id"), @Index("user_id")}
)
public class Answer {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "answer_id")
    private long answerId;
    
    @ColumnInfo(name = "question_id")
    private long questionId;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "content")
    private String content;
    
    @ColumnInfo(name = "rating")
    private int rating; // 1-5 stars
    
    @ColumnInfo(name = "upvotes")
    private int upvotes;
    
    @ColumnInfo(name = "downvotes")
    private int downvotes;
    
    @ColumnInfo(name = "is_accepted")
    private boolean isAccepted;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructor
    public Answer() {
        this.rating = 0;
        this.upvotes = 0;
        this.downvotes = 0;
        this.isAccepted = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
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
