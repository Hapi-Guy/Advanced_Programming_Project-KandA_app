package com.kna.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kna.android.R;
import com.kna.android.ui.main.MainActivity;
import com.kna.android.ui.viewmodel.AuthViewModel;
import com.kna.android.util.SessionManager;

/**
 * Login Activity - migrated from desktop LoginController
 */
public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private RadioGroup userTypeRadioGroup;
    private MaterialButton btnLogin;
    private TextView btnRegister;
    
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = SessionManager.getInstance(this);
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        
        // Set click listeners
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        
        // Observe login result
        authViewModel.getLoginResult().observe(this, result -> {
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void login() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        boolean isAdmin = userTypeRadioGroup.getCheckedRadioButtonId() == R.id.radioAdmin;
        
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }
        
        // Call ViewModel to login
        authViewModel.login(email, password, isAdmin);
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
