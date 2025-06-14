package com.example.appdocbao.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private HomeViewModel homeViewModel;
    private RecyclerView rvTrendingArticles;
    private RecyclerView rvRecentArticles;
    private TextView btnSeeAllTop;
    private TextView btnSeeAllRecent;
    private BottomNavigationView bottomNavigationView;

    private TrendingArticleAdapter trendingArticleAdapter;
    private ArticleAdapter recentArticleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize views
        initViews();
        
        // Set up adapters
        setupAdapters();
        
        // Set up observers
        setupObservers();
        
        // Set up click listeners
        setupClickListeners();
        
        // Set up bottom navigation
        setupBottomNavigation();
        
        // Load data
        loadData();
    }

    private void initViews() {
        rvTrendingArticles = findViewById(R.id.rv_trending_articles);
        rvRecentArticles = findViewById(R.id.rv_recent_articles);
        
        btnSeeAllTop = findViewById(R.id.btn_see_all_top);
        btnSeeAllRecent = findViewById(R.id.btn_see_all_recent);
        
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupAdapters() {
        // Trending Articles Adapter
        trendingArticleAdapter = new TrendingArticleAdapter(new ArrayList<>());
        rvTrendingArticles.setAdapter(trendingArticleAdapter);
        LinearLayoutManager trendingLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTrendingArticles.setLayoutManager(trendingLayoutManager);
        
        // Recent Articles Adapter
        recentArticleAdapter = new ArticleAdapter(new ArrayList<>());
        rvRecentArticles.setAdapter(recentArticleAdapter);
        rvRecentArticles.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        // Observe trending articles
        homeViewModel.getTrendingArticles().observe(this, articles -> {
            trendingArticleAdapter.updateArticles(articles);
        });
        
        // Observe recent articles
        homeViewModel.getRecentArticles().observe(this, articles -> {
            recentArticleAdapter.updateArticles(articles);
        });
        
        // Observe loading state
        homeViewModel.getIsLoading().observe(this, isLoading -> {
            // Show or hide loading indicator
        });
        
        // Observe error messages
        homeViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        // Setup see all buttons
        btnSeeAllTop.setOnClickListener(v -> {
            // Open all top articles
            Toast.makeText(this, "See all top articles clicked", Toast.LENGTH_SHORT).show();
        });
        
        btnSeeAllRecent.setOnClickListener(v -> {
            // Open all recent articles
            Toast.makeText(this, "See all recent articles clicked", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Already on home
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_bookmarks) {
                    Intent intent = new Intent(HomeActivity.this, BookmarksActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void loadData() {
        homeViewModel.loadTrendingArticles();
        homeViewModel.loadRecentArticles();
        
        // Load all category articles
        homeViewModel.loadAllCategories();
    }
} 