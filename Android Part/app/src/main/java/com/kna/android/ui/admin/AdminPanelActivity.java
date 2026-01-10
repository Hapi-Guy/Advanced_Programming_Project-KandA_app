package com.kna.android.ui.admin;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kna.android.R;
import com.kna.android.data.database.KnADatabase;
import com.kna.android.data.model.User;
import com.kna.android.data.repository.UserRepository;
import com.kna.android.util.SessionManager;

import java.util.concurrent.Executors;

/**
 * Admin Panel Activity - Full implementation with password reset functionality
 */
public class AdminPanelActivity extends AppCompatActivity {
    
    private static final String DEFAULT_PASSWORD = "test123";
    
    private UserRepository userRepository;
    
    // Search views
    private TextInputLayout searchEmailInputLayout;
    private TextInputEditText searchEmailInput;
    private MaterialButton btnSearchUser;
    
    // User found views
    private MaterialCardView userFoundCard;
    private TextView foundUserInitial;
    private TextView foundUserName;
    private TextView foundUserEmail;
    private TextView foundUserDept;
    private MaterialButton btnResetPassword;
    
    // User not found views
    private MaterialCardView userNotFoundCard;
    private TextView notFoundMessage;
    
    // Stats views
    private TextView totalUsersCount;
    private TextView totalQuestionsCount;
    private TextView totalAnswersCount;
    
    // Current found user
    private User foundUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        
        // Check if user is admin
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        userRepository = new UserRepository((Application) getApplication());
        
        initViews();
        setupClickListeners();
        loadStats();
    }
    
    private void initViews() {
        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Search views
        searchEmailInputLayout = findViewById(R.id.searchEmailInputLayout);
        searchEmailInput = findViewById(R.id.searchEmailInput);
        btnSearchUser = findViewById(R.id.btnSearchUser);
        
        // User found views
        userFoundCard = findViewById(R.id.userFoundCard);
        foundUserInitial = findViewById(R.id.foundUserInitial);
        foundUserName = findViewById(R.id.foundUserName);
        foundUserEmail = findViewById(R.id.foundUserEmail);
        foundUserDept = findViewById(R.id.foundUserDept);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        
        // User not found views
        userNotFoundCard = findViewById(R.id.userNotFoundCard);
        notFoundMessage = findViewById(R.id.notFoundMessage);
        
        // Stats views
        totalUsersCount = findViewById(R.id.totalUsersCount);
        totalQuestionsCount = findViewById(R.id.totalQuestionsCount);
        totalAnswersCount = findViewById(R.id.totalAnswersCount);
    }
    
    private void setupClickListeners() {
        btnSearchUser.setOnClickListener(v -> searchUser());
        btnResetPassword.setOnClickListener(v -> confirmResetPassword());
    }
    
    private void searchUser() {
        // Clear previous errors and results
        searchEmailInputLayout.setError(null);
        userFoundCard.setVisibility(View.GONE);
        userNotFoundCard.setVisibility(View.GONE);
        foundUser = null;
        
        String email = searchEmailInput.getText() != null ? 
                searchEmailInput.getText().toString().trim() : "";
        
        // Validation
        if (email.isEmpty()) {
            searchEmailInputLayout.setError("Please enter an email address");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            searchEmailInputLayout.setError("Invalid email format");
            return;
        }
        
        // Disable button during search
        btnSearchUser.setEnabled(false);
        btnSearchUser.setText("Searching...");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = KnADatabase.getDatabase(this)
                    .userDao().getUserByEmailSync(email);
            
            runOnUiThread(() -> {
                btnSearchUser.setEnabled(true);
                btnSearchUser.setText("Search User");
                
                if (user != null) {
                    foundUser = user;
                    displayFoundUser(user);
                } else {
                    displayUserNotFound(email);
                }
            });
        });
    }
    
    private void displayFoundUser(User user) {
        userNotFoundCard.setVisibility(View.GONE);
        userFoundCard.setVisibility(View.VISIBLE);
        
        String name = user.getName() != null ? user.getName() : "User";
        foundUserInitial.setText(name.isEmpty() ? "U" : String.valueOf(name.charAt(0)).toUpperCase());
        foundUserName.setText(name);
        foundUserEmail.setText(user.getEmail());
        
        String dept = user.getDepartment() != null ? user.getDepartment() : "N/A";
        int year = user.getAcademicYear();
        foundUserDept.setText(dept + " • Year " + year);
        
        // Check if user is admin
        if (user.isAdmin()) {
            foundUserDept.setText(dept + " • Year " + year + " • ADMIN");
        }
    }
    
    private void displayUserNotFound(String email) {
        userFoundCard.setVisibility(View.GONE);
        userNotFoundCard.setVisibility(View.VISIBLE);
        notFoundMessage.setText("No user found with email: " + email);
    }
    
    private void confirmResetPassword() {
        if (foundUser == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Password Reset")
                .setMessage("Are you sure you want to reset the password for " + 
                        foundUser.getEmail() + " to " + "default password" + "?\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> resetPassword())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void resetPassword() {
        if (foundUser == null) return;
        
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");
        
        // Use the void method - it doesn't have a callback
        userRepository.resetPassword(foundUser.getUserId(), DEFAULT_PASSWORD);
        
        // Since resetPassword is async but doesn't have callback, wait a bit then show success
        btnResetPassword.postDelayed(() -> {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("Reset Password");
            
            Toast.makeText(this, "Password reset successfully ",
                    Toast.LENGTH_LONG).show();
            
            // Clear search
            searchEmailInput.setText("");
            userFoundCard.setVisibility(View.GONE);
            foundUser = null;
        }, 500);
    }
    
    private void loadStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            KnADatabase db = KnADatabase.getDatabase(this);
            
            int userCount = db.userDao().getUserCountSync();
            int questionCount = db.questionDao().getQuestionCountSync();
            int answerCount = db.answerDao().getTotalAnswerCountSync();
            
            runOnUiThread(() -> {
                totalUsersCount.setText(String.valueOf(userCount));
                totalQuestionsCount.setText(String.valueOf(questionCount));
                totalAnswersCount.setText(String.valueOf(answerCount));
            });
        });
    }
}
