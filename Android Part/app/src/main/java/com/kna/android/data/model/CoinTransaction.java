package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Coin transaction history entity
 */
@Entity(
    tableName = "coin_transactions",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id")}
)
public class CoinTransaction {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    private long transactionId;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "amount")
    private int amount;
    
    @ColumnInfo(name = "transaction_type")
    private String transactionType; // EARN, SPEND, PURCHASE
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "balance_after")
    private int balanceAfter;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public CoinTransaction() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
