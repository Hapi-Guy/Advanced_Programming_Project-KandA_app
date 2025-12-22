package com.kna.model;

import java.sql.Timestamp;

/**
 * User model representing a user in the KnA application
 */
public class User {
    private int userId;
    private String email;
    private String phone;
    private String passwordHash;
    private String name;
    private String department;
    private int academicYear;
    private int coins;
    private int reputation;
    private int totalQuestions;
    private int totalAnswers;
    private int acceptedAnswers;
    private boolean isActive;
    private boolean isAdmin;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public User() {}

    public User(int userId, String email, String name, String department, int academicYear) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.department = department;
        this.academicYear = academicYear;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Alias for getUserId (for consistency)
    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    // Alias for getTotalAnswers
    public int getAnswersGiven() {
        return totalAnswers;
    }

    // Alias for getTotalQuestions
    public int getQuestionsAsked() {
        return totalQuestions;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calculate acceptance rate
     */
    public double getAcceptanceRate() {
        if (totalAnswers == 0) return 0.0;
        return (double) acceptedAnswers / totalAnswers * 100.0;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", coins=" + coins +
                ", reputation=" + reputation +
                '}';
    }
}
