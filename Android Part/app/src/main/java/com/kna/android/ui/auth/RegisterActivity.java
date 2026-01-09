package com.kna.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kna.android.R;
import com.kna.android.ui.viewmodel.AuthViewModel;
import com.kna.android.util.Constants;

/**
 * Register Activity - migrated from desktop RegisterController
 */
public class RegisterActivity extends AppCompatActivity {
    
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText phoneInput;
    private AutoCompleteTextView departmentDropdown;
    private AutoCompleteTextView yearDropdown;
    private MaterialButton btnRegister;
    private TextView btnLogin;
    
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        departmentDropdown = findViewById(R.id.departmentDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        
        // Setup dropdowns
        setupDepartmentDropdown();
        setupYearDropdown();
        
        // Set click listeners
        btnRegister.setOnClickListener(v -> register());
        btnLogin.setOnClickListener(v -> {
            finish();
        });
        
        // Observe register result
        authViewModel.getRegisterResult().observe(this, result -> {
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                finish(); // Go back to login
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupDepartmentDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            Constants.DEPARTMENTS
        );
        departmentDropdown.setAdapter(adapter);
    }
    
    private void setupYearDropdown() {
        String[] years = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            years
        );
        yearDropdown.setAdapter(adapter);
    }
    
    private void register() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String department = departmentDropdown.getText().toString();
        String yearStr = yearDropdown.getText().toString();
        
        // Validate inputs
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }
        
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }
        
        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            return;
        }
        
        if (department.isEmpty()) {
            Toast.makeText(this, "Please select department", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (yearStr.isEmpty()) {
            Toast.makeText(this, "Please select academic year", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int year = Integer.parseInt(yearStr);
        
        // Call ViewModel to register
        authViewModel.register(email, password, name, department, year, phone);
    }
}
