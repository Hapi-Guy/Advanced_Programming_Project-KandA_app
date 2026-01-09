package com.kna.android.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kna.android.data.dao.AnswerDao;
import com.kna.android.data.dao.AnswerVoteDao;
import com.kna.android.data.dao.CoinTransactionDao;
import com.kna.android.data.dao.NotificationDao;
import com.kna.android.data.dao.QuestionDao;
import com.kna.android.data.dao.UserDao;
import com.kna.android.data.model.Answer;
import com.kna.android.data.model.AnswerVote;
import com.kna.android.data.model.CoinTransaction;
import com.kna.android.data.model.Notification;
import com.kna.android.data.model.Question;
import com.kna.android.data.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database - migrated from desktop DatabaseManager.java
 * Singleton pattern for database access
 * Equivalent to desktop's kna_database.db with 6 main tables
 */
@Database(
    entities = {
        User.class,
        Question.class,
        Answer.class,
        AnswerVote.class,
        Notification.class,
        CoinTransaction.class
    },
    version = 1,
    exportSchema = true
)
public abstract class KnADatabase extends RoomDatabase {
    
    // DAOs
    public abstract UserDao userDao();
    public abstract QuestionDao questionDao();
    public abstract AnswerDao answerDao();
    public abstract AnswerVoteDao answerVoteDao();
    public abstract NotificationDao notificationDao();
    public abstract CoinTransactionDao coinTransactionDao();
    
    // Singleton instance
    private static volatile KnADatabase INSTANCE;
    
    // Thread pool for database operations (prevents blocking main thread)
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    /**
     * Get singleton database instance
     */
    public static KnADatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (KnADatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            KnADatabase.class,
                            "kna_database"
                    )
                    .addCallback(roomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Callback to populate database with sample data on creation
     * Equivalent to desktop's database initialization
     */
    private static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Populate database with sample data in background
            databaseWriteExecutor.execute(() -> {
                // Database is created, sample data will be added via DataSeeder
            });
        }
    };
}
