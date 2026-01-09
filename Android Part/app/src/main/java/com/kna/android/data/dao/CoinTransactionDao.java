package com.kna.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kna.android.data.model.CoinTransaction;

import java.util.List;

/**
 * CoinTransaction DAO - migrated from desktop CoinDAO.java
 */
@Dao
public interface CoinTransactionDao {
    
    @Insert
    long insert(CoinTransaction transaction);
    
    /**
     * Get all transactions for user
     */
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<CoinTransaction>> getTransactionsForUser(long userId);
    
    /**
     * Get transactions by type
     */
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND transaction_type = :type ORDER BY created_at DESC")
    LiveData<List<CoinTransaction>> getTransactionsByType(long userId, String type);
    
    /**
     * Get recent transactions (limited)
     */
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<CoinTransaction>> getRecentTransactions(long userId, int limit);
}
