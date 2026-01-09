package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kna.android.data.model.AnswerVote;

import java.util.List;

/**
 * AnswerVote DAO for managing upvotes/downvotes
 */
@Dao
public interface AnswerVoteDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AnswerVote vote);
    
    @Delete
    void delete(AnswerVote vote);
    
    /**
     * Get user's vote for specific answer
     */
    @Query("SELECT * FROM answer_votes WHERE answer_id = :answerId AND user_id = :userId LIMIT 1")
    AnswerVote getUserVote(long answerId, long userId);
    
    /**
     * Check if user has voted on answer
     */
    @Query("SELECT COUNT(*) > 0 FROM answer_votes WHERE answer_id = :answerId AND user_id = :userId")
    boolean hasUserVoted(long answerId, long userId);
    
    /**
     * Get all votes for an answer
     */
    @Query("SELECT * FROM answer_votes WHERE answer_id = :answerId")
    LiveData<List<AnswerVote>> getVotesForAnswer(long answerId);
    
    /**
     * Delete user's vote for answer
     */
    @Query("DELETE FROM answer_votes WHERE answer_id = :answerId AND user_id = :userId")
    void deleteUserVote(long answerId, long userId);
}
