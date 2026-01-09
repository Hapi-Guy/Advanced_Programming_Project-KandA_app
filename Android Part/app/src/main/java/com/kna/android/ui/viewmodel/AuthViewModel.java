package com.kna.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kna.android.data.model.User;
import com.kna.android.data.repository.UserRepository;
import com.kna.android.util.SessionManager;

import java.util.concurrent.ExecutionException;

/**
 * AuthViewModel - handles login and registration logic
 * Migrated from desktop LoginController and RegisterController
 */
public class AuthViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    
    private final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> registerResult = new MutableLiveData<>();
    
    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }
    
    /**
     * Login user
     */
    public void login(String email, String password, boolean isAdmin) {
        new Thread(() -> {
            try {
                UserRepository.LoginResult result = userRepository.login(email, password, isAdmin).get();
                
                if (result.success) {
                    // Save session
                    sessionManager.saveSession(result.user);
                }
                
                loginResult.postValue(new AuthResult(result.success, result.message, result.user));
            } catch (ExecutionException | InterruptedException e) {
                loginResult.postValue(new AuthResult(false, "Login failed: " + e.getMessage(), null));
            }
        }).start();
    }
    
    /**
     * Register new user
     */
    public void register(String email, String password, String name, String department, 
                        int academicYear, String phoneNumber) {
        new Thread(() -> {
            try {
                UserRepository.RegisterResult result = userRepository.register(
                    email, password, name, department, academicYear, phoneNumber
                ).get();
                
                registerResult.postValue(new AuthResult(result.success, result.message, result.user));
            } catch (ExecutionException | InterruptedException e) {
                registerResult.postValue(new AuthResult(false, "Registration failed: " + e.getMessage(), null));
            }
        }).start();
    }
    
    public LiveData<AuthResult> getLoginResult() {
        return loginResult;
    }
    
    public LiveData<AuthResult> getRegisterResult() {
        return registerResult;
    }
    
    /**
     * Auth result wrapper class
     */
    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final User user;
        
        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }
}
