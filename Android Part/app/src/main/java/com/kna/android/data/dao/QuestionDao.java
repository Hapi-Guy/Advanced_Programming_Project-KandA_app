package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kna.android.data.model.Question;
import com.kna.android.data.model.QuestionWithUser;

import java.util.List;

/**
 * Question DAO - migrated from desktop QuestionDAO.java
 */
@Dao
public interface QuestionDao {
    
    @Insert
    long insert(Question question);
    
    @Update
    void update(Question question);
    
    @Delete
    void delete(Question question);
    
    @Query("SELECT * FROM questions WHERE question_id = :questionId")
    LiveData<Question> getQuestionById(long questionId);
    
    @Query("SELECT * FROM questions WHERE question_id = :questionId")
    Question getQuestionByIdSync(long questionId);
    
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "WHERE q.question_id = :questionId")
    LiveData<QuestionWithUser> getQuestionWithUserById(long questionId);
    
    /**
     * Get all questions with user info - sorted by urgent first, then newest
     * This is the main feed query for Quora-style home screen
     */
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "ORDER BY q.is_urgent DESC, q.created_at DESC")
    LiveData<List<QuestionWithUser>> getAllQuestionsWithUser();
    
    /**
     * Get questions by category
     */
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "WHERE q.category = :category " +
           "ORDER BY q.is_urgent DESC, q.created_at DESC")
    LiveData<List<QuestionWithUser>> getQuestionsByCategory(String category);
    
    /**
     * Get urgent questions only
     */
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "WHERE q.is_urgent = 1 " +
           "ORDER BY q.created_at DESC")
    LiveData<List<QuestionWithUser>> getUrgentQuestions();
    
    /**
     * Get questions by department (for "My Dept" filter)
     */
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "WHERE u.department = :department " +
           "ORDER BY q.is_urgent DESC, q.created_at DESC")
    LiveData<List<QuestionWithUser>> getQuestionsByDepartment(String department);
    
    /**
     * Get questions asked by specific user
     */
    @Query("SELECT * FROM questions WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Question>> getQuestionsByUser(long userId);
    
    /**
     * Get answered/unanswered questions by user
     */
    @Query("SELECT * FROM questions WHERE user_id = :userId AND is_answered = :isAnswered ORDER BY created_at DESC")
    LiveData<List<Question>> getQuestionsByUserFiltered(long userId, boolean isAnswered);
    
    /**
     * Get unevaluated questions count (for validation rule: max 5)
     */
    @Query("SELECT COUNT(*) FROM questions WHERE user_id = :userId AND accepted_answer_id IS NULL")
    int getUnevaluatedQuestionCount(long userId);
    
    /**
     * Update answer count
     */
    @Query("UPDATE questions SET answer_count = answer_count + 1, updated_at = :timestamp WHERE question_id = :questionId")
    void incrementAnswerCount(long questionId, long timestamp);
    
    /**
     * Update view count
     */
    @Query("UPDATE questions SET view_count = view_count + 1 WHERE question_id = :questionId")
    void incrementViewCount(long questionId);
    
    /**
     * Mark question as answered with accepted answer
     */
    @Query("UPDATE questions SET is_answered = 1, accepted_answer_id = :answerId, updated_at = :timestamp WHERE question_id = :questionId")
    void markAsAnswered(long questionId, long answerId, long timestamp);
    
    /**
     * Search questions by title or description
     */
    @Query("SELECT q.*, u.name as user_name, u.department as user_department, u.academic_year as user_academicYear " +
           "FROM questions q " +
           "INNER JOIN users u ON q.user_id = u.user_id " +
           "WHERE q.title LIKE '%' || :query || '%' OR q.description LIKE '%' || :query || '%' " +
           "ORDER BY q.is_urgent DESC, q.created_at DESC")
    LiveData<List<QuestionWithUser>> searchQuestions(String query);
    
    /**
     * Get total question count for admin stats
     */
    @Query("SELECT COUNT(*) FROM questions")
    int getQuestionCountSync();
}
