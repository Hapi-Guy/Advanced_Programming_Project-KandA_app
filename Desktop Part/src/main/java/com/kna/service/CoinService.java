package com.kna.service;

import com.kna.dao.CoinDAO;
import com.kna.dao.UserDAO;
import com.kna.model.CoinPurchase;
import com.kna.model.CoinTransaction;
import com.kna.model.User;
import com.kna.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * CoinService - Business logic for coin management
 */
public class CoinService {
    
    private final CoinDAO coinDAO;
    private final UserDAO userDAO;

    public CoinService() {
        this.coinDAO = new CoinDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Purchase coins
     */
    public boolean purchaseCoins(int coinsAmount, BigDecimal price) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new Exception("User not logged in");
        }
        
        try {
            // Calculate new balance
            int newBalance = currentUser.getCoins() + coinsAmount;
            
            // Update user coins
            userDAO.updateCoins(currentUser.getUserId(), newBalance);
            
            // Record transaction
            CoinTransaction transaction = new CoinTransaction(
                currentUser.getUserId(),
                coinsAmount,
                "purchased",
                "Purchased " + coinsAmount + " Coins",
                newBalance
            );
            int transactionId = coinDAO.createTransaction(transaction);
            
            // Record purchase
            CoinPurchase purchase = new CoinPurchase(
                currentUser.getUserId(),
                coinsAmount,
                price
            );
            purchase.setTransactionId(transactionId);
            coinDAO.recordPurchase(purchase);
            
            // Update session
            SessionManager.getInstance().updateCoins(newBalance);
            
            return true;
            
        } catch (SQLException e) {
            throw new Exception("Failed to purchase coins: " + e.getMessage());
        }
    }

    /**
     * Get coin packages
     */
    public static class CoinPackage {
        public int coins;
        public BigDecimal price;
        public String name;
        
        public CoinPackage(String name, int coins, BigDecimal price) {
            this.name = name;
            this.coins = coins;
            this.price = price;
        }
    }
    
    /**
     * Get available coin packages
     */
    public CoinPackage[] getPackages() {
        return new CoinPackage[] {
            new CoinPackage("Starter", 50, new BigDecimal("50.00")),
            new CoinPackage("Basic", 100, new BigDecimal("90.00")),
            new CoinPackage("Standard", 250, new BigDecimal("200.00")),
            new CoinPackage("Premium", 500, new BigDecimal("375.00")),
            new CoinPackage("Ultimate", 1000, new BigDecimal("700.00"))
        };
    }
}
