package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Vote entity for answer upvotes/downvotes
 * Ensures one vote per user per answer (unique constraint)
 */
@Entity(
    tableName = "answer_votes",
    foreignKeys = {
        @ForeignKey(
            entity = Answer.class,
            parentColumns = "answer_id",
            childColumns = "answer_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "user_id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"answer_id", "user_id"}, unique = true),
        @Index("user_id")
    }
)
public class AnswerVote {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "vote_id")
    private long voteId;
    
    @ColumnInfo(name = "answer_id")
    private long answerId;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "vote_type")
    private int voteType; // 1 for upvote, -1 for downvote
    
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public AnswerVote() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getVoteId() {
        return voteId;
    }

    public void setVoteId(long voteId) {
        this.voteId = voteId;
    }

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
