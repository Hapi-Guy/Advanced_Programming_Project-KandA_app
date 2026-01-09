package com.kna.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kna.android.data.model.User;
import com.kna.android.data.repository.UserRepository;
import com.kna.android.util.SessionManager;

import java.util.List;

/**
 * ProfileViewModel - handles user profile display and leaderboard
 * Migrated from desktop ProfileController and LeaderboardController
 */
public class ProfileViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }
    
    /**
     * Get current user profile
     */
    public LiveData<User> getCurrentUserProfile() {
        long userId = sessionManager.getCurrentUserId();
        return userRepository.getUserById(userId);
    }
    
    /**
     * Get leaderboard (top users by reputation)
     */
    public LiveData<List<User>> getLeaderboard() {
        return userRepository.getAllUsersByReputation();
    }
    
    /**
     * Get top N users
     */
    public LiveData<List<User>> getTopUsers(int limit) {
        return userRepository.getTopUsersByReputation(limit);
    }
    
    /**
     * Update user profile
     */
    public void updateProfile(User user) {
        userRepository.updateUser(user);
    }
}
