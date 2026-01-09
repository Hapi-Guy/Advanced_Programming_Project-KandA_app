package com.kna.android.util;

import android.content.Context;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.*;

/**
 * Utility class to seed the database with sample test data.
 * Run this once to populate the database for immediate testing.
 * 
 * Usage:
 * - Call DataSeeder.seedDatabase(context) from MainActivity onCreate
 * - Or create a debug menu item to trigger seeding
 */
public class DataSeeder {
    
    private static final String TAG = "DataSeeder";
    
    /**
     * Seeds the database with sample users, questions, and answers.
     * This method is idempotent - safe to call multiple times.
     * 
     * @param context Application context
     */
    public static void seedDatabase(Context context) {
        KnADatabase db = KnADatabase.getDatabase(context);
        
        KnADatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Check if already seeded
                if (db.userDao().getUserByEmail("test@kuet.ac.bd") != null) {
                    android.util.Log.d(TAG, "Database already seeded. Skipping...");
                    return;
                }
                
                // Create sample users
                User[] users = createSampleUsers(db);
                
                // Create sample questions
                Question[] questions = createSampleQuestions(db, users);
                
                // Create sample answers
                createSampleAnswers(db, users, questions);
                
                android.util.Log.d(TAG, "Database seeded successfully!");
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error seeding database", e);
            }
        });
    }
    
    private static User[] createSampleUsers(KnADatabase db) {
        User[] users = new User[5];
        
        // User 1: Regular student
        users[0] = new User();
        users[0].setEmail("test@kuet.ac.bd");
        users[0].setPasswordHash("password123"); // Plain text password
        users[0].setName("Rafiq Ahmed");
        users[0].setDepartment("CSE");
        users[0].setAcademicYear(3);
        users[0].setCoins(150);
        users[0].setReputation(100);
        users[0].setAdmin(false);
        users[0].setCreatedAt(System.currentTimeMillis());
        long user1Id = db.userDao().insert(users[0]);
        users[0].setUserId(user1Id);
        
        // User 2: Active contributor
        users[1] = new User();
        users[1].setEmail("sakib@kuet.ac.bd");
        users[1].setPasswordHash("password123"); // Plain text password
        users[1].setName("Sakib Rahman");
        users[1].setDepartment("EEE");
        users[1].setAcademicYear(4);
        users[1].setCoins(250);
        users[1].setReputation(300);
        users[1].setAdmin(false);
        users[1].setCreatedAt(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L); // 7 days ago
        long user2Id = db.userDao().insert(users[1]);
        users[1].setUserId(user2Id);
        
        // User 3: New student
        users[2] = new User();
        users[2].setEmail("nabila@kuet.ac.bd");
        users[2].setPasswordHash("password123"); // Plain text password
        users[2].setName("Nabila Islam");
        users[2].setDepartment("ME");
        users[2].setAcademicYear(1);
        users[2].setCoins(80);
        users[2].setReputation(20);
        users[2].setAdmin(false);
        users[2].setCreatedAt(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L); // 2 days ago
        long user3Id = db.userDao().insert(users[2]);
        users[2].setUserId(user3Id);
        
        // User 4: Expert (high reputation)
        users[3] = new User();
        users[3].setEmail("karim@kuet.ac.bd");
        users[3].setPasswordHash("password123"); // Plain text password
        users[3].setName("Dr. Karim Hossain");
        users[3].setDepartment("CSE");
        users[3].setAcademicYear(5);
        users[3].setCoins(500);
        users[3].setReputation(850);
        users[3].setAdmin(false);
        users[3].setCreatedAt(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L); // 30 days ago
        long user4Id = db.userDao().insert(users[3]);
        users[3].setUserId(user4Id);
        
        // User 5: Admin
        users[4] = new User();
        users[4].setEmail("admin@kuet.ac.bd");
        users[4].setPasswordHash("admin123"); // Plain text password
        users[4].setName("Admin User");
        users[4].setDepartment("Admin");
        users[4].setAcademicYear(5);
        users[4].setCoins(1000);
        users[4].setReputation(1000);
        users[4].setAdmin(true);
        users[4].setCreatedAt(System.currentTimeMillis() - 60 * 24 * 60 * 60 * 1000L); // 60 days ago
        long user5Id = db.userDao().insert(users[4]);
        users[4].setUserId(user5Id);
        
        return users;
    }
    
    private static Question[] createSampleQuestions(KnADatabase db, User[] users) {
        Question[] questions = new Question[8];
        
        // Question 1: Urgent CSE question
        questions[0] = new Question();
        questions[0].setUserId(users[0].getUserId());
        questions[0].setTitle("How to implement binary search tree in Java?");
        questions[0].setDescription("I'm struggling with BST implementation for my data structures assignment. Can someone explain the insert and delete operations with proper code examples?");
        questions[0].setCategory("CSE");
        questions[0].setCoinReward(30);
        questions[0].setUrgent(true);
        questions[0].setCreatedAt(System.currentTimeMillis() - 2 * 60 * 60 * 1000L); // 2 hours ago
        long q1Id = db.questionDao().insert(questions[0]);
        questions[0].setQuestionId(q1Id);
        
        // Question 2: Regular EEE question
        questions[1] = new Question();
        questions[1].setUserId(users[1].getUserId());
        questions[1].setTitle("Best resources for learning circuit analysis?");
        questions[1].setDescription("Looking for good textbooks or online courses for AC/DC circuit analysis. Any recommendations?");
        questions[1].setCategory("EEE");
        questions[1].setCoinReward(20);
        questions[1].setUrgent(false);
        questions[1].setCreatedAt(System.currentTimeMillis() - 5 * 60 * 60 * 1000L); // 5 hours ago
        long q2Id = db.questionDao().insert(questions[1]);
        questions[1].setQuestionId(q2Id);
        
        // Question 3: Campus life
        questions[2] = new Question();
        questions[2].setUserId(users[2].getUserId());
        questions[2].setTitle("Where is the best place to study on campus?");
        questions[2].setDescription("As a first-year student, I'm looking for quiet study spots. Library is always crowded. Any suggestions?");
        questions[2].setCategory("URP");
        questions[2].setCoinReward(20);
        questions[2].setUrgent(false);
        questions[2].setCreatedAt(System.currentTimeMillis() - 8 * 60 * 60 * 1000L); // 8 hours ago
        long q3Id = db.questionDao().insert(questions[2]);
        questions[2].setQuestionId(q3Id);
        
        // Question 4: Technical Discussion
        questions[3] = new Question();
        questions[3].setUserId(users[0].getUserId());
        questions[3].setTitle("Android Room vs Realm Database - Which is better?");
        questions[3].setDescription("I'm building an Android app and confused between Room and Realm. What are the pros and cons of each?");
        questions[3].setCategory("CSE");
        questions[3].setCoinReward(20);
        questions[3].setUrgent(false);
        questions[3].setCreatedAt(System.currentTimeMillis() - 12 * 60 * 60 * 1000L); // 12 hours ago
        long q4Id = db.questionDao().insert(questions[3]);
        questions[3].setQuestionId(q4Id);
        
        // Question 5: Urgent exam prep
        questions[4] = new Question();
        questions[4].setUserId(users[2].getUserId());
        questions[4].setTitle("Calculus midterm tomorrow! Need help with integration by parts");
        questions[4].setDescription("I have an exam tomorrow and I'm stuck on integration by parts. Can someone explain the LIATE rule with examples?");
        questions[4].setCategory("MATH");
        questions[4].setCoinReward(30);
        questions[4].setUrgent(true);
        questions[4].setCreatedAt(System.currentTimeMillis() - 3 * 60 * 60 * 1000L); // 3 hours ago
        long q5Id = db.questionDao().insert(questions[4]);
        questions[4].setQuestionId(q5Id);
        
        // Question 6: Career advice
        questions[5] = new Question();
        questions[5].setUserId(users[1].getUserId());
        questions[5].setTitle("Internship opportunities for CSE students?");
        questions[5].setDescription("I'm in my 3rd year and looking for software engineering internships. Which companies hire from KUET?");
        questions[5].setCategory("CSE");
        questions[5].setCoinReward(20);
        questions[5].setUrgent(false);
        questions[5].setCreatedAt(System.currentTimeMillis() - 24 * 60 * 60 * 1000L); // 1 day ago
        long q6Id = db.questionDao().insert(questions[5]);
        questions[5].setQuestionId(q6Id);
        
        // Question 7: General query
        questions[6] = new Question();
        questions[6].setUserId(users[0].getUserId());
        questions[6].setTitle("How to register for semester courses online?");
        questions[6].setDescription("The course registration portal is confusing. Can someone guide me through the steps?");
        questions[6].setCategory("HUM");
        questions[6].setCoinReward(20);
        questions[6].setUrgent(false);
        questions[6].setCreatedAt(System.currentTimeMillis() - 36 * 60 * 60 * 1000L); // 1.5 days ago
        long q7Id = db.questionDao().insert(questions[6]);
        questions[6].setQuestionId(q7Id);
        
        // Question 8: Already answered question
        questions[7] = new Question();
        questions[7].setUserId(users[2].getUserId());
        questions[7].setTitle("Best programming language to learn in 2026?");
        questions[7].setDescription("I want to start learning programming. Should I go with Python, Java, or JavaScript?");
        questions[7].setCategory("CSE");
        questions[7].setCoinReward(20);
        questions[7].setUrgent(false);
        questions[7].setCreatedAt(System.currentTimeMillis() - 48 * 60 * 60 * 1000L); // 2 days ago
        long q8Id = db.questionDao().insert(questions[7]);
        questions[7].setQuestionId(q8Id);
        
        return questions;
    }
    
    private static void createSampleAnswers(KnADatabase db, User[] users, Question[] questions) {
        // Answer for Question 1 (BST)
        Answer answer1 = new Answer();
        answer1.setQuestionId(questions[0].getQuestionId());
        answer1.setUserId(users[3].getUserId());
        answer1.setContent("Here's a simple BST implementation:\n\n```java\nclass Node {\n    int data;\n    Node left, right;\n    Node(int d) { data = d; }\n}\n\nclass BST {\n    Node root;\n    \n    void insert(int key) {\n        root = insertRec(root, key);\n    }\n    \n    Node insertRec(Node root, int key) {\n        if (root == null) return new Node(key);\n        if (key < root.data) root.left = insertRec(root.left, key);\n        else if (key > root.data) root.right = insertRec(root.right, key);\n        return root;\n    }\n}\n```");
        answer1.setCreatedAt(System.currentTimeMillis() - 1 * 60 * 60 * 1000L); // 1 hour ago
        long answer1Id = db.answerDao().insert(answer1);
        
        // Answer for Question 2 (Circuit Analysis)
        Answer answer2 = new Answer();
        answer2.setQuestionId(questions[1].getQuestionId());
        answer2.setUserId(users[3].getUserId());
        answer2.setContent("I highly recommend 'Fundamentals of Electric Circuits' by Alexander & Sadiku. Also check out Khan Academy's circuit analysis playlist on YouTube.");
        answer2.setCreatedAt(System.currentTimeMillis() - 3 * 60 * 60 * 1000L);
        db.answerDao().insert(answer2);
        
        // Answer for Question 3 (Study spots)
        Answer answer3 = new Answer();
        answer3.setQuestionId(questions[2].getQuestionId());
        answer3.setUserId(users[1].getUserId());
        answer3.setContent("Try the 4th floor of the AB building. Very quiet and has good air circulation. Also the CSE department reading room is great if you're in CSE.");
        answer3.setCreatedAt(System.currentTimeMillis() - 6 * 60 * 60 * 1000L);
        db.answerDao().insert(answer3);
        
        // Two answers for Question 8 (Programming language)
        Answer answer4 = new Answer();
        answer4.setQuestionId(questions[7].getQuestionId());
        answer4.setUserId(users[3].getUserId());
        answer4.setContent("Python is the best choice for beginners. Easy syntax, vast libraries, and great for data science, web development, and automation. Start with Python, then learn others based on your interests.");
        answer4.setRating(5);
        answer4.setCreatedAt(System.currentTimeMillis() - 40 * 60 * 60 * 1000L);
        long answer4Id = db.answerDao().insert(answer4);
        
        Answer answer5 = new Answer();
        answer5.setQuestionId(questions[7].getQuestionId());
        answer5.setUserId(users[1].getUserId());
        answer5.setContent("JavaScript is more relevant in 2026. With Node.js you can do both frontend and backend. Plus it's essential for web development which is huge in the job market.");
        answer5.setRating(3);
        answer5.setCreatedAt(System.currentTimeMillis() - 38 * 60 * 60 * 1000L);
        db.answerDao().insert(answer5);
        
        // Mark Question 8 as accepted (best answer)
        db.questionDao().markAsAnswered(questions[7].getQuestionId(), answer4Id, System.currentTimeMillis());
        
        // Create some votes
        AnswerVote vote1 = new AnswerVote();
        vote1.setAnswerId(answer1Id);
        vote1.setUserId(users[0].getUserId());
        vote1.setVoteType(1); // 1 for upvote
        vote1.setCreatedAt(System.currentTimeMillis());
        db.answerVoteDao().insert(vote1);
        
        AnswerVote vote2 = new AnswerVote();
        vote2.setAnswerId(answer4Id);
        vote2.setUserId(users[0].getUserId());
        vote2.setVoteType(1); // 1 for upvote
        vote2.setCreatedAt(System.currentTimeMillis());
        db.answerVoteDao().insert(vote2);
        
        // Update answer vote counts
        db.answerDao().incrementUpvotes(answer1Id);
        db.answerDao().incrementUpvotes(answer4Id);
        
        // Create notifications
        Notification notif1 = new Notification();
        notif1.setUserId(users[0].getUserId());
        notif1.setMessage("Dr. Karim Hossain answered your question about BST implementation");
        notif1.setType("ANSWER");
        notif1.setReferenceId(questions[0].getQuestionId());
        notif1.setRead(false);
        notif1.setCreatedAt(System.currentTimeMillis() - 1 * 60 * 60 * 1000L);
        db.notificationDao().insert(notif1);
        
        Notification notif2 = new Notification();
        notif2.setUserId(users[2].getUserId());
        notif2.setMessage("Your answer was accepted! You earned 20 coins and 50 reputation.");
        notif2.setType("ACCEPTED_ANSWER");
        notif2.setReferenceId(questions[7].getQuestionId());
        notif2.setRead(false);
        notif2.setCreatedAt(System.currentTimeMillis() - 10 * 60 * 60 * 1000L);
        db.notificationDao().insert(notif2);
    }
}
