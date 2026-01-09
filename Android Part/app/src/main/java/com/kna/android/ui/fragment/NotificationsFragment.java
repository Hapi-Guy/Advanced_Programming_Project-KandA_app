package com.kna.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Notifications Fragment - placeholder
 * TODO: Implement full notification functionality
 */
public class NotificationsFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText("Notifications\n\n(Coming soon)");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        return textView;
    }
}
