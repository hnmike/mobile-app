package com.example.appdocbao.ui.categories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.newslist.NewsListActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.example.appdocbao.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "CategoriesActivity";
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private CategoriesViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_categories);

            // Initialize UI components
            initViews();
            
            // Set up RecyclerView
            setupRecyclerView();

            // Initialize ViewModel
            initViewModel();

            // Set up bottom navigation
            setupBottomNavigation();

            // Load categories
            viewModel.loadCategories();
            
            // Check if a specific category was selected
            String selectedCategoryId = getIntent().getStringExtra("SELECTED_CATEGORY_ID");
            if (selectedCategoryId != null) {
                // Handle selected category after categories are loaded
                viewModel.getCategories().observe(this, categories -> {
                    for (Category category : categories) {
                        if (category.getId().equals(selectedCategoryId)) {
                            // Open the category in NewsListActivity
                            onCategoryClick(category);
                            break;
                        }
                    }
                    // Remove observer after first categories load
                    viewModel.getCategories().removeObservers(this);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing CategoriesActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing application: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void initViews() {
        try {
            recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
            progressBar = findViewById(R.id.progressBar);
            
            // Check if bottomNavigation is in the XML layout
            // If it's a LinearLayout with this ID
            View navigation = findViewById(R.id.bottomNavigation);
            if (navigation instanceof BottomNavigationView) {
                bottomNavigationView = (BottomNavigationView) navigation;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }
    
    private void setupRecyclerView() {
        try {
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerViewCategories.setLayoutManager(layoutManager);
            categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
            recyclerViewCategories.setAdapter(categoryAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
        }
    }
    
    private void initViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);

            // Observe data changes
            viewModel.getCategories().observe(this, this::updateCategories);
            viewModel.getIsLoading().observe(this, this::setLoadingState);
            viewModel.getErrorMessage().observe(this, this::showError);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModel: " + e.getMessage(), e);
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView == null) {
                Log.w(TAG, "Bottom navigation view is null, possibly not in the layout");
                return;
            }
            
            bottomNavigationView.setSelectedItemId(R.id.nav_categories);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                try {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        // Navigate to Home screen
                        Intent intent = new Intent(CategoriesActivity.this, com.example.appdocbao.ui.home.HomeActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (itemId == R.id.nav_categories) {
                        // Đã ở Categories rồi
                        return true;
                    } else if (itemId == R.id.nav_bookmarks) {
                        // Chuyển đến BookmarksActivity
                        Intent intent = new Intent(CategoriesActivity.this, BookmarksActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        // Kiểm tra login và chuyển đến trang phù hợp
                        if (isUserLoggedIn()) {
                            // Nếu đã đăng nhập, mở ProfileActivity
                            Intent profileIntent = new Intent(CategoriesActivity.this, ProfileActivity.class);
                            startActivity(profileIntent);
                        } else {
                            // Nếu chưa đăng nhập, mở SignInActivity
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
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập của người dùng
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    private boolean isUserLoggedIn() {
        try {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }

    private void updateCategories(List<Category> categories) {
        try {
            if (categoryAdapter != null && categories != null) {
                categoryAdapter.updateCategories(categories);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating categories: " + e.getMessage(), e);
        }
    }

    private void setLoadingState(boolean isLoading) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting loading state: " + e.getMessage(), e);
        }
    }

    private void showError(String errorMessage) {
        try {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + e.getMessage(), e);
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        try {
            Intent intent = new Intent(this, NewsListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to news list: " + e.getMessage(), e);
            Toast.makeText(this, "Could not open category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 