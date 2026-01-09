package com.kna.android.ui.question;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.kna.android.R;
import com.kna.android.ui.viewmodel.AskQuestionViewModel;
import com.kna.android.util.Constants;

import android.widget.TextView;

/**
 * Ask Question Activity - migrated from desktop AskQuestionController
 */
public class AskQuestionActivity extends AppCompatActivity {
    
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private AutoCompleteTextView categoryDropdown;
    private MaterialCheckBox urgentCheckbox;
    private TextView coinCostText;
    private MaterialButton btnPost;
    private MaterialButton btnCancel;
    
    private AskQuestionViewModel askQuestionViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);
        
        // Initialize ViewModel
        askQuestionViewModel = new ViewModelProvider(this).get(AskQuestionViewModel.class);
        
        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        urgentCheckbox = findViewById(R.id.urgentCheckbox);
        coinCostText = findViewById(R.id.coinCostText);
        btnPost = findViewById(R.id.btnPost);
        btnCancel = findViewById(R.id.btnCancel);
        
        // Setup category dropdown
        setupCategoryDropdown();
        
        // Update coin cost when urgent checkbox changes
        urgentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCoinCost(isChecked);
        });
        
        // Set click listeners
        btnPost.setOnClickListener(v -> postQuestion());
        btnCancel.setOnClickListener(v -> finish());
        
        // Observe result
        askQuestionViewModel.getAskQuestionResult().observe(this, result -> {
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            Constants.CATEGORIES
        );
        categoryDropdown.setAdapter(adapter);
    }
    
    private void updateCoinCost(boolean isUrgent) {
        int cost = isUrgent ? Constants.URGENT_QUESTION_COST : Constants.BASE_QUESTION_COST;
        coinCostText.setText("Cost: " + cost + " coins");
    }
    
    private void postQuestion() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
        String category = categoryDropdown.getText().toString();
        boolean isUrgent = urgentCheckbox.isChecked();
        
        // Validate inputs
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }
        
        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            return;
        }
        
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call ViewModel to post question
        askQuestionViewModel.askQuestion(title, description, category, isUrgent);
    }
}
