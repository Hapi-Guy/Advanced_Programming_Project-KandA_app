package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kna.android.data.model.User;

import java.util.List;

/**
 * User DAO - migrated from desktop UserDAO.java
 * All queries return LiveData for reactive UI updates
 */
@Dao
public interface UserDao {
    
    @Insert
    long insert(User user);
    
    @Update
    void update(User user);
    
    @Delete
    void delete(User user);
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(long userId);
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserByIdSync(long userId);
    
    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE email = :email AND password_hash = :passwordHash LIMIT 1")
    User authenticate(String email, String passwordHash);
    
    @Query("SELECT * FROM users WHERE is_admin = 1")
    LiveData<List<User>> getAllAdmins();
    
    @Query("SELECT * FROM users ORDER BY reputation DESC LIMIT :limit")
    LiveData<List<User>> getTopUsersByReputation(int limit);
    
    @Query("SELECT * FROM users ORDER BY reputation DESC")
    LiveData<List<User>> getAllUsersByReputation();
    
    @Query("UPDATE users SET coins = coins + :amount WHERE user_id = :userId")
    void addCoins(long userId, int amount);
    
    @Query("UPDATE users SET coins = coins - :amount WHERE user_id = :userId")
    void deductCoins(long userId, int amount);
    
    @Query("UPDATE users SET reputation = reputation + :amount WHERE user_id = :userId")
    void addReputation(long userId, int amount);
    
    @Query("UPDATE users SET total_questions = total_questions + 1 WHERE user_id = :userId")
    void incrementTotalQuestions(long userId);
    
    @Query("UPDATE users SET total_answers = total_answers + 1 WHERE user_id = :userId")
    void incrementTotalAnswers(long userId);
    
    @Query("UPDATE users SET accepted_answers = accepted_answers + 1 WHERE user_id = :userId")
    void incrementAcceptedAnswers(long userId);
    
    @Query("UPDATE users SET password_hash = :newPasswordHash WHERE user_id = :userId")
    void updatePassword(long userId, String newPasswordHash);
    
    @Query("UPDATE users SET last_login = :timestamp WHERE user_id = :userId")
    void updateLastLogin(long userId, long timestamp);
    
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int emailExists(String email);
    
    @Query("SELECT * FROM users WHERE department = :department ORDER BY reputation DESC")
    LiveData<List<User>> getUsersByDepartment(String department);
}
