package com.kna.util;

import java.sql.SQLException;
import java.util.List;

import com.kna.dao.UserDAO;
import com.kna.model.User;

/**
 * Utility to view all user passwords (PLAIN TEXT)
 * Run this as a standalone program to see all passwords
 */
public class PasswordViewer {
    
    public static void main(String[] args) {
        System.out.println("=== KnA User Passwords (Plain Text) ===\n");
        
        try {
            // Initialize database
            DatabaseManager.getInstance();
            
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers();
            
            if (users.isEmpty()) {
                System.out.println("No users found in database.");
                return;
            }
            
            System.out.printf("%-5s %-30s %-25s %-15s%n", "ID", "Email", "Name", "Password");
            System.out.println("-".repeat(80));
            
            for (User user : users) {
                System.out.printf("%-5d %-30s %-25s %-15s%n",
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPasswordHash()
                );
            }
            
            System.out.println("\nTotal users: " + users.size());
            
        } catch (SQLException e) {
            System.err.println("Error reading users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().closeConnection();
        }
    }
}
