package com.kna.dao;

import com.kna.model.CoinTransaction;
import com.kna.model.CoinPurchase;
import com.kna.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CoinDAO - Data Access Object for Coin transactions and purchases
 */
public class CoinDAO {
    
    private final DatabaseManager dbManager;

    public CoinDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Record a coin transaction
     */
    public int createTransaction(CoinTransaction transaction) throws SQLException {
        String sql = "INSERT INTO coin_transactions (user_id, amount, transaction_type, description, " +
                     "reference_id, reference_type, balance_after) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            transaction.getUserId(),
            transaction.getAmount(),
            transaction.getTransactionType(),
            transaction.getDescription(),
            transaction.getReferenceId(),
            transaction.getReferenceType(),
            transaction.getBalanceAfter()
        );
    }

    /**
     * Get transactions by user ID
     */
    public List<CoinTransaction> getTransactionsByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT * FROM coin_transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        
        List<CoinTransaction> transactions = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        }
        
        return transactions;
    }

    /**
     * Get all transactions (for admin)
     */
    public List<CoinTransaction> getAllTransactions(int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM coin_transactions ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        List<CoinTransaction> transactions = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        }
        
        return transactions;
    }

    /**
     * Record coin purchase
     */
    public int recordPurchase(CoinPurchase purchase) throws SQLException {
        String sql = "INSERT INTO coin_purchases (user_id, coins_purchased, amount_paid, payment_method, " +
                     "payment_status, transaction_id) VALUES (?, ?, ?, ?, ?, ?)";
        
        return dbManager.executeUpdateWithKey(sql,
            purchase.getUserId(),
            purchase.getCoinsPurchased(),
            purchase.getAmountPaid(),
            purchase.getPaymentMethod(),
            purchase.getPaymentStatus(),
            purchase.getTransactionId()
        );
    }

    /**
     * Get purchases by user ID
     */
    public List<CoinPurchase> getPurchasesByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM coin_purchases WHERE user_id = ? ORDER BY created_at DESC";
        
        List<CoinPurchase> purchases = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
        }
        
        return purchases;
    }

    /**
     * Get all purchases (for admin)
     */
    public List<CoinPurchase> getAllPurchases() throws SQLException {
        String sql = "SELECT * FROM coin_purchases ORDER BY created_at DESC";
        
        List<CoinPurchase> purchases = new ArrayList<>();
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
        }
        
        return purchases;
    }

    /**
     * Get total coins earned by user
     */
    public int getTotalCoinsEarned(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM coin_transactions " +
                     "WHERE user_id = ? AND transaction_type = 'earned'";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Get total coins spent by user
     */
    public int getTotalCoinsSpent(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ABS(amount)), 0) FROM coin_transactions " +
                     "WHERE user_id = ? AND transaction_type = 'spent'";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Get total coins purchased by user
     */
    public int getTotalCoinsPurchased(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(coins_purchased), 0) FROM coin_purchases " +
                     "WHERE user_id = ? AND payment_status = 'completed'";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Extract CoinTransaction from ResultSet
     */
    private CoinTransaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        CoinTransaction transaction = new CoinTransaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setAmount(rs.getInt("amount"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setDescription(rs.getString("description"));
        
        int refId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            transaction.setReferenceId(refId);
        }
        
        transaction.setReferenceType(rs.getString("reference_type"));
        transaction.setBalanceAfter(rs.getInt("balance_after"));
        transaction.setCreatedAt(rs.getTimestamp("created_at"));
        return transaction;
    }

    /**
     * Extract CoinPurchase from ResultSet
     */
    private CoinPurchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        CoinPurchase purchase = new CoinPurchase();
        purchase.setPurchaseId(rs.getInt("purchase_id"));
        purchase.setUserId(rs.getInt("user_id"));
        purchase.setCoinsPurchased(rs.getInt("coins_purchased"));
        purchase.setAmountPaid(rs.getBigDecimal("amount_paid"));
        purchase.setPaymentMethod(rs.getString("payment_method"));
        purchase.setPaymentStatus(rs.getString("payment_status"));
        
        int transId = rs.getInt("transaction_id");
        if (!rs.wasNull()) {
            purchase.setTransactionId(transId);
        }
        
        purchase.setCreatedAt(rs.getTimestamp("created_at"));
        return purchase;
    }
}
