package com.kna.android.util;

/**
 * Constants - migrated from desktop app business rules
 * Contains all the magic numbers and business logic constants
 */
public class Constants {
    
    // Coin costs (from QuestionService.java)
    public static final int BASE_QUESTION_COST = 20;
    public static final int URGENT_QUESTION_COST = 30;
    
    // Reputation points (from AnswerService.java)
    public static final int REPUTATION_PER_ACCEPTED = 50;
    
    // Validation limits
    public static final int MAX_UNEVALUATED_QUESTIONS = 5;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_ACADEMIC_YEAR = 1;
    public static final int MAX_ACADEMIC_YEAR = 5;
    
    // Urgent answer bonus time window (minutes)
    public static final int URGENT_BONUS_WINDOW_MINUTES = 30;
    
    // Rating threshold for penalty
    public static final int RATING_PENALTY_THRESHOLD = 2;
    
    // Department options
    public static final String[] DEPARTMENTS = {
        "CSE", "EEE", "CE", "ME", "IPE", "URP", "ARCH", "BME"
    };
    
    // Question categories (KUET Departments)
    public static final String[] CATEGORIES = {
        "CSE", "EEE", "MATH", "HUM", "CHEM", "CE", "ME", "IEM", 
        "URP", "BECM", "ECE", "ESE", "BME", "LE", "TE", "ARCH", 
        "MSE", "CHE", "MTE", "PHY"
    };
    
    // Notification types
    public static final String NOTIFICATION_TYPE_ANSWER = "ANSWER";
    public static final String NOTIFICATION_TYPE_VOTE = "VOTE";
    public static final String NOTIFICATION_TYPE_ACCEPTED = "ACCEPTED";
    public static final String NOTIFICATION_TYPE_COIN = "COIN";
    
    // Transaction types
    public static final String TRANSACTION_TYPE_EARN = "EARN";
    public static final String TRANSACTION_TYPE_SPEND = "SPEND";
    public static final String TRANSACTION_TYPE_PURCHASE = "PURCHASE";
    
    // Initial coins for new users
    public static final int INITIAL_COINS = 100;
    
    // Coin purchase packages (from CoinPurchaseController)
    public static final int PACKAGE_SMALL_COINS = 50;
    public static final double PACKAGE_SMALL_PRICE = 1.99;
    
    public static final int PACKAGE_MEDIUM_COINS = 150;
    public static final double PACKAGE_MEDIUM_PRICE = 4.99;
    
    public static final int PACKAGE_LARGE_COINS = 500;
    public static final double PACKAGE_LARGE_PRICE = 14.99;
    
    // SharedPreferences keys
    public static final String PREF_NAME = "KnAPrefs";
    public static final String PREF_USER_ID = "userId";
    public static final String PREF_IS_LOGGED_IN = "isLoggedIn";
    public static final String PREF_IS_ADMIN = "isAdmin";
    
    // Request codes
    public static final int REQUEST_CODE_PICK_IMAGE = 100;
    public static final int REQUEST_CODE_TAKE_PHOTO = 101;
    
    // Intent extras
    public static final String EXTRA_QUESTION_ID = "question_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_ANSWER_ID = "answer_id";
}
