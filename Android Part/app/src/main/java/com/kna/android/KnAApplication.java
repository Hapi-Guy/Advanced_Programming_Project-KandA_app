package com.kna.android;

import android.app.Application;

import com.kna.android.data.database.KnADatabase;
import com.kna.android.util.DataSeeder;

/**
 * Application class - initializes database and seeds sample data
 */
public class KnAApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize database
        KnADatabase.getDatabase(this);
        
        // Seed database with sample data
        DataSeeder.seedDatabase(this);
    }
}
