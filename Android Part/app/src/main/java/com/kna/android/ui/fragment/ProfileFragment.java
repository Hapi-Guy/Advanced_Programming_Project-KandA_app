package com.kna.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kna.android.ui.auth.LoginActivity;
import com.kna.android.util.SessionManager;

/**
 * Profile Fragment - placeholder
 * TODO: Implement full profile with user stats
 */
public class ProfileFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        TextView textView = new TextView(getContext());
        textView.setText("Profile\n\n(Coming soon)");
        textView.setTextSize(18);
        textView.setPadding(0, 0, 0, 32);
        
        Button logoutButton = new Button(getContext());
        logoutButton.setText("Logout");
        logoutButton.setOnClickListener(v -> {
            SessionManager.getInstance(getContext()).clearSession();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        layout.addView(textView);
        layout.addView(logoutButton);
        
        return layout;
    }
}
