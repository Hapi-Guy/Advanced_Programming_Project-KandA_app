package com.kna.android.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

/**
 * User entity mapping to 'users' table
 * Migrated from desktop JavaFX User.java model
 */
@Entity(tableName = "users")
public class User {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "password_hash")
    private String passwordHash;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "department")
    private String department;
    
    @ColumnInfo(name = "academic_year")
    private int academicYear;
    
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    
    @ColumnInfo(name = "coins")
    private int coins;
    
    @ColumnInfo(name = "reputation")
    private int reputation;
    
    @ColumnInfo(name = "total_questions")
    private int totalQuestions;
    
    @ColumnInfo(name = "total_answers")
    private int totalAnswers;
    
    @ColumnInfo(name = "accepted_answers")
    private int acceptedAnswers;
    
    @ColumnInfo(name = "is_admin")
    private boolean isAdmin;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "last_login")
    private long lastLogin;

    // Constructor
    public User() {
        this.coins = 100; // Initial coins as per desktop app
        this.reputation = 0;
        this.totalQuestions = 0;
        this.totalAnswers = 0;
        this.acceptedAnswers = 0;
        this.isAdmin = false;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }

    // Business logic method from desktop app
    public double getAcceptanceRate() {
        return totalAnswers == 0 ? 0.0 : (acceptedAnswers / (double) totalAnswers * 100.0);
    }

    // Getters and Setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public int getAcceptedAnswers() {
        return acceptedAnswers;
    }

    public void setAcceptedAnswers(int acceptedAnswers) {
        this.acceptedAnswers = acceptedAnswers;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}
