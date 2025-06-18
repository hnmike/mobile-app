package com.example.appdocbao.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appdocbao.R;
import com.example.appdocbao.data.News;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.example.appdocbao.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình chính (Home) của ứng dụng, hiển thị danh sách các danh mục tin tức
 * và các bài viết nổi bật trong mỗi danh mục.
 */
public class HomeActivity extends AppCompatActivity implements HomeCategoriesAdapter.OnNewsClickListener {

    private static final String TAG = "HomeActivity";

    // UI Components
    private RecyclerView rvCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;

    // Adapter
    private HomeCategoriesAdapter homeCategoriesAdapter;

    // ViewModel
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize UI
        initializeUI();
        
        // Setup observers
        setupObservers();
        
        // Setup navigation
        setupBottomNavigation();

        // Load data
        viewModel.loadDataFromApi();
    }

    private void initializeUI() {
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Trang chủ");
        }

        // Initialize views
        rvCategories = findViewById(R.id.rvCategories);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshData();
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Setup RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        homeCategoriesAdapter = new HomeCategoriesAdapter(this, new ArrayList<>(), new HashMap<>(), this);
        rvCategories.setAdapter(homeCategoriesAdapter);
    }

    private void setupObservers() {
        // Observe categories
        viewModel.getCategories().observe(this, categories -> {
            Log.d(TAG, "Categories updated: " + categories.size());
        });

        // Observe category news map
        viewModel.getCategoryNewsMap().observe(this, categoryNewsMap -> {
            Log.d(TAG, "Category news map updated: " + categoryNewsMap.size());
            homeCategoriesAdapter.updateData(viewModel.getCategories().getValue(), categoryNewsMap);
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.w(TAG, "Bottom navigation view is null");
            return;
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            try {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_bookmarks) {
                    Intent intent = new Intent(HomeActivity.this, BookmarksActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    if (isUserLoggedIn()) {
                        Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                    } else {
                        Intent loginIntent = new Intent();
                        loginIntent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                        startActivity(loginIntent);
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Error in navigation: " + e.getMessage(), e);
                return false;
            }
        });
    }

    private boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    @Override
    public void onNewsClick(News news) {
        try {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ARTICLE_ID, String.valueOf(news.getId()));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening news detail: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở bài viết", Toast.LENGTH_SHORT).show();
        }
    }
}
