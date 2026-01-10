package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kna.android.data.model.Answer;
import com.kna.android.data.model.AnswerWithUser;

import java.util.List;

/**
 * Answer DAO - migrated from desktop AnswerDAO.java
 */
@Dao
public interface AnswerDao {
    
    @Insert
    long insert(Answer answer);
    
    @Update
    void update(Answer answer);
    
    @Delete
    void delete(Answer answer);
    
    @Query("SELECT * FROM answers WHERE answer_id = :answerId")
    LiveData<Answer> getAnswerById(long answerId);
    
    @Query("SELECT * FROM answers WHERE answer_id = :answerId")
    Answer getAnswerByIdSync(long answerId);
    
    /**
     * Get all answers for a question with user info
     * Uses LEFT JOIN to ensure answers are ALWAYS returned even if user data is missing
     */
    @Query("SELECT a.*, " +
           "COALESCE(u.name, 'Unknown User') as user_name, " +
           "COALESCE(u.department, 'N/A') as user_department, " +
           "COALESCE(u.academic_year, 0) as user_academicYear, " +
           "COALESCE(u.reputation, 0) as user_reputation " +
           "FROM answers a " +
           "LEFT JOIN users u ON a.user_id = u.user_id " +
           "WHERE a.question_id = :questionId " +
           "ORDER BY a.is_accepted DESC, (a.upvotes - a.downvotes) DESC, a.created_at ASC")
    LiveData<List<AnswerWithUser>> getAnswersForQuestion(long questionId);
    
    /**
     * Get answers by specific user
     */
    @Query("SELECT * FROM answers WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Answer>> getAnswersByUser(long userId);
    
    /**
     * Mark answer as accepted
     */
    @Query("UPDATE answers SET is_accepted = 1 WHERE answer_id = :answerId")
    void markAsAccepted(long answerId);
    
    /**
     * Unmark all other answers for the same question (only one accepted answer)
     */
    @Query("UPDATE answers SET is_accepted = 0 WHERE question_id = :questionId AND answer_id != :answerId")
    void unmarkOthersAsAccepted(long questionId, long answerId);
    
    /**
     * Update rating
     */
    @Query("UPDATE answers SET rating = :rating WHERE answer_id = :answerId")
    void updateRating(long answerId, int rating);
    
    /**
     * Increment upvote count
     */
    @Query("UPDATE answers SET upvotes = upvotes + 1 WHERE answer_id = :answerId")
    void incrementUpvotes(long answerId);
    
    /**
     * Decrement upvote count
     */
    @Query("UPDATE answers SET upvotes = upvotes - 1 WHERE answer_id = :answerId")
    void decrementUpvotes(long answerId);
    
    /**
     * Increment downvote count
     */
    @Query("UPDATE answers SET downvotes = downvotes + 1 WHERE answer_id = :answerId")
    void incrementDownvotes(long answerId);
    
    /**
     * Decrement downvote count
     */
    @Query("UPDATE answers SET downvotes = downvotes - 1 WHERE answer_id = :answerId")
    void decrementDownvotes(long answerId);
    
    /**
     * Get answer count for question
     */
    @Query("SELECT COUNT(*) FROM answers WHERE question_id = :questionId")
    int getAnswerCount(long questionId);
    
    /**
     * Get total answer count for admin stats
     */
    @Query("SELECT COUNT(*) FROM answers")
    int getTotalAnswerCountSync();
}
