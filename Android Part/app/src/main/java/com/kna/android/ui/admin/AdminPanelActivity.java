package com.kna.android.ui.admin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Admin Panel Activity - migrated from desktop AdminPanelController
 * Placeholder for admin functionality
 */
public class AdminPanelActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("Admin Panel\n\n(Coming soon)");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
    }
}
