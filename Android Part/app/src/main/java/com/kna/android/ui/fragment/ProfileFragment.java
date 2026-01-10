package com.kna.android.ui.fragment;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kna.android.R;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.User;
import com.kna.android.data.repository.UserRepository;
import com.kna.android.ui.admin.AdminPanelActivity;
import com.kna.android.ui.auth.LoginActivity;
import com.kna.android.util.SessionManager;

import java.util.concurrent.Executors;

/**
 * Profile Fragment - Full implementation with profile editing and password change
 */
public class ProfileFragment extends Fragment {
    
    private SessionManager sessionManager;
    private UserRepository userRepository;
    
    // Header views
    private TextView avatarInitial;
    private TextView displayName;
    private TextView displayEmail;
    private TextView coinsCount;
    private TextView reputationCount;
    private TextView questionsCount;
    
    // Edit profile views
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout phoneInputLayout;
    private MaterialButton btnSaveProfile;
    
    // Change password views
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout currentPasswordInputLayout;
    private TextInputLayout newPasswordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private MaterialButton btnChangePassword;
    
    // Other buttons
    private MaterialButton btnAdminPanel;
    private MaterialButton btnLogout;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = SessionManager.getInstance(requireContext());
        userRepository = new UserRepository((Application) requireActivity().getApplication());
        
        initViews(view);
        loadUserData();
        setupClickListeners();
    }
    
    private void initViews(View view) {
        // Header views
        avatarInitial = view.findViewById(R.id.avatarInitial);
        displayName = view.findViewById(R.id.displayName);
        displayEmail = view.findViewById(R.id.displayEmail);
        coinsCount = view.findViewById(R.id.coinsCount);
        reputationCount = view.findViewById(R.id.reputationCount);
        questionsCount = view.findViewById(R.id.questionsCount);
        
        // Edit profile views
        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        phoneInputLayout = view.findViewById(R.id.phoneInputLayout);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        
        // Change password views
        currentPasswordInput = view.findViewById(R.id.currentPasswordInput);
        newPasswordInput = view.findViewById(R.id.newPasswordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        currentPasswordInputLayout = view.findViewById(R.id.currentPasswordInputLayout);
        newPasswordInputLayout = view.findViewById(R.id.newPasswordInputLayout);
        confirmPasswordInputLayout = view.findViewById(R.id.confirmPasswordInputLayout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        
        // Other buttons
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        // Show admin panel button only for admins
        if (sessionManager.isAdmin()) {
            btnAdminPanel.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadUserData() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        } else {
            // Load from database
            long userId = sessionManager.getCurrentUserId();
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = KnADatabase.getDatabase(requireContext())
                        .userDao().getUserByIdSync(userId);
                if (user != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        sessionManager.setCurrentUser(user);
                        updateUI(user);
                    });
                }
            });
        }
    }
    
    private void updateUI(User user) {
        // Header
        String name = user.getName() != null ? user.getName() : "User";
        avatarInitial.setText(name.isEmpty() ? "U" : String.valueOf(name.charAt(0)).toUpperCase());
        displayName.setText(name);
        displayEmail.setText(user.getEmail());
        coinsCount.setText(String.valueOf(user.getCoins()));
        reputationCount.setText(String.valueOf(user.getReputation()));
        questionsCount.setText(String.valueOf(user.getTotalQuestions()));
        
        // Edit fields
        nameInput.setText(user.getName());
        emailInput.setText(user.getEmail());
        phoneInput.setText(user.getPhoneNumber());
    }
    
    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnAdminPanel.setOnClickListener(v -> openAdminPanel());
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private void saveProfile() {
        // Clear previous errors
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        phoneInputLayout.setError(null);
        
        String name = getText(nameInput);
        String email = getText(emailInput);
        String phone = getText(phoneInput);
        
        // Validation
        if (name.isEmpty()) {
            nameInputLayout.setError("Name is required");
            return;
        }
        
        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            return;
        }
        
        if (!isValidEmail(email)) {
            emailInputLayout.setError("Invalid email format");
            return;
        }
        
        if (!email.endsWith("@kuet.ac.bd")) {
            emailInputLayout.setError("Must be a KUET email (@kuet.ac.bd)");
            return;
        }
        
        // Disable button during save
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");
        
        long userId = sessionManager.getCurrentUserId();
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Check if email is already taken by another user
                User existingUser = KnADatabase.getDatabase(requireContext())
                        .userDao().getUserByEmailSync(email);
                
                if (existingUser != null && existingUser.getUserId() != userId) {
                    showError("Email is already taken by another user");
                    return;
                }
                
                // Update user
                User currentUser = KnADatabase.getDatabase(requireContext())
                        .userDao().getUserByIdSync(userId);
                
                if (currentUser != null) {
                    currentUser.setName(name);
                    currentUser.setEmail(email);
                    currentUser.setPhoneNumber(phone);
                    
                    KnADatabase.getDatabase(requireContext())
                            .userDao().update(currentUser);
                    
                    // Update session
                    sessionManager.setCurrentUser(currentUser);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("Save Profile");
                            updateUI(currentUser);
                            Toast.makeText(getContext(), "Profile updated successfully", 
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                showError("Failed to update profile: " + e.getMessage());
            }
        });
    }
    
    private void changePassword() {
        // Clear previous errors
        currentPasswordInputLayout.setError(null);
        newPasswordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
        
        String currentPassword = getText(currentPasswordInput);
        String newPassword = getText(newPasswordInput);
        String confirmPassword = getText(confirmPasswordInput);
        
        // Validation
        if (currentPassword.isEmpty()) {
            currentPasswordInputLayout.setError("Current password is required");
            return;
        }
        
        if (newPassword.isEmpty()) {
            newPasswordInputLayout.setError("New password is required");
            return;
        }
        
        if (newPassword.length() < 6) {
            newPasswordInputLayout.setError("Password must be at least 6 characters");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            return;
        }
        
        if (currentPassword.equals(newPassword)) {
            newPasswordInputLayout.setError("New password must be different from current");
            return;
        }
        
        // Disable button during operation
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Changing...");
        
        long userId = sessionManager.getCurrentUserId();
        
        // Use the Future-based API
        try {
            userRepository.changePassword(userId, currentPassword, newPassword).get();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password");
                    // Clear password fields
                    currentPasswordInput.setText("");
                    newPasswordInput.setText("");
                    confirmPasswordInput.setText("");
                    Toast.makeText(getContext(), "Password changed successfully", 
                            Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password");
                    currentPasswordInputLayout.setError("Current password is incorrect");
                });
            }
        }
    }
    
    private void openAdminPanel() {
        Intent intent = new Intent(getContext(), AdminPanelActivity.class);
        startActivity(intent);
    }
    
    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    
    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Profile");
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
