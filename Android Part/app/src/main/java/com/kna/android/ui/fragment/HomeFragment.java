package com.kna.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kna.android.R;
import com.kna.android.ui.adapter.QuestionAdapter;
import com.kna.android.ui.question.AskQuestionActivity;
import com.kna.android.ui.question.QuestionDetailActivity;
import com.kna.android.ui.viewmodel.HomeViewModel;
import com.kna.android.util.Constants;

/**
 * Home Fragment - Quora-style question feed
 * Migrated from desktop DashboardController
 */
public class HomeFragment extends Fragment {
    
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView questionRecyclerView;
    private ChipGroup filterChipGroup;
    private View askBar;
    
    private HomeViewModel homeViewModel;
    private QuestionAdapter questionAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        questionRecyclerView = view.findViewById(R.id.questionRecyclerView);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        askBar = view.findViewById(R.id.askBar);
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.refreshQuestions();
            swipeRefreshLayout.setRefreshing(false);
        });
        
        // Setup filter chips
        setupFilterChips();
        
        // Ask bar click listener
        askBar.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AskQuestionActivity.class));
        });
        
        // Observe questions
        homeViewModel.getQuestions().observe(getViewLifecycleOwner(), questions -> {
            questionAdapter.submitList(questions);
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh questions when returning to this fragment
        homeViewModel.refreshQuestions();
    }
    
    private void setupRecyclerView() {
        questionAdapter = new QuestionAdapter(questionWithUser -> {
            // Navigate to question detail
            Intent intent = new Intent(getContext(), QuestionDetailActivity.class);
            intent.putExtra(Constants.EXTRA_QUESTION_ID, questionWithUser.question.getQuestionId());
            startActivity(intent);
        });
        
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        questionRecyclerView.setAdapter(questionAdapter);
    }
    
    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.chipForYou) {
                homeViewModel.setFilter("all");
            } else if (checkedId == R.id.chipFollowing) {
                homeViewModel.setFilter("following");
            } else if (checkedId == R.id.chipMyDept) {
                homeViewModel.setFilter("my_dept");
            } else if (checkedId == R.id.chipUrgent) {
                homeViewModel.setFilter("urgent");
            }
        });
    }
}
