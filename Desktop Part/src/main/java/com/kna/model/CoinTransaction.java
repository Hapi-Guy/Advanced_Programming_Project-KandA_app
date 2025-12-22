package com.kna.model;

import java.sql.Timestamp;

/**
 * CoinTransaction model representing coin transactions
 */
public class CoinTransaction {
    private int transactionId;
    private int userId;
    private int amount;
    private String transactionType; // earned, spent, purchased, refund
    private String description;
    private Integer referenceId;
    private String referenceType;
    private int balanceAfter;
    private Timestamp createdAt;

    // Constructors
    public CoinTransaction() {}

    public CoinTransaction(int userId, int amount, String transactionType, String description, int balanceAfter) {
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.balanceAfter = balanceAfter;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CoinTransaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", balanceAfter=" + balanceAfter +
                '}';
    }
}
