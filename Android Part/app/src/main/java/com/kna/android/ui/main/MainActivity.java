package com.kna.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kna.android.R;
import com.kna.android.ui.fragment.HomeFragment;
import com.kna.android.ui.fragment.LeaderboardFragment;
import com.kna.android.ui.fragment.NotificationsFragment;
import com.kna.android.ui.fragment.ProfileFragment;
import com.kna.android.ui.question.AskQuestionActivity;

/**
 * Main Activity with Bottom Navigation - migrated from desktop DashboardController
 * Hosts fragments for Home, Notifications, Leaderboard, and Profile
 */
public class MainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);
        
        // Setup toolbar click listeners
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            // TODO: Implement search
        });
        
        findViewById(R.id.btnAddQuestion).setOnClickListener(v -> {
            startActivity(new Intent(this, AskQuestionActivity.class));
        });
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_notifications) {
            fragment = new NotificationsFragment();
        } else if (itemId == R.id.navigation_ask) {
            startActivity(new Intent(this, AskQuestionActivity.class));
            return false;
        } else if (itemId == R.id.navigation_leaderboard) {
            fragment = new LeaderboardFragment();
        } else if (itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        }
        
        return fragment != null && loadFragment(fragment);
    }
    
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
