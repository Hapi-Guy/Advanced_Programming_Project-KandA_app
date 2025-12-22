package com.kna.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * CoinPurchase model representing coin purchase transactions
 */
public class CoinPurchase {
    private int purchaseId;
    private int userId;
    private int coinsPurchased;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String paymentStatus;
    private Integer transactionId;
    private Timestamp createdAt;

    // Constructors
    public CoinPurchase() {}

    public CoinPurchase(int userId, int coinsPurchased, BigDecimal amountPaid) {
        this.userId = userId;
        this.coinsPurchased = coinsPurchased;
        this.amountPaid = amountPaid;
        this.paymentMethod = "simulated";
        this.paymentStatus = "completed";
    }

    // Getters and Setters
    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCoinsPurchased() {
        return coinsPurchased;
    }

    public void setCoinsPurchased(int coinsPurchased) {
        this.coinsPurchased = coinsPurchased;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
