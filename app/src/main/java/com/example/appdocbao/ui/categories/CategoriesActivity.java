package com.example.appdocbao.ui.categories;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

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
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private CategoriesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        initializeComponents();
    }

<<<<<<< HEAD
    private void initializeComponents() {
        initViews();
        setupRecyclerView();
        initViewModel();
        setupBottomNavigation();
        loadData();
=======
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
>>>>>>> origin/fixbug_information
    }
    
    private void initViews() {
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        
        View navigation = findViewById(R.id.bottomNavigation);
        if (navigation instanceof BottomNavigationView) {
            bottomNavigationView = (BottomNavigationView) navigation;
        }
    }
    
    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(this, 2));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        observeViewModelData();
    }
    
    private void observeViewModelData() {
        viewModel.getCategories().observe(this, this::updateCategories);
        viewModel.getIsLoading().observe(this, this::setLoadingState);
    }
    
    private void loadData() {
        viewModel.loadCategories();
    }
    
    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_categories);
<<<<<<< HEAD
            bottomNavigationView.setOnItemSelectedListener(this::handleNavigationItemSelected);
=======
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
>>>>>>> origin/fixbug_information
        }
    }
    
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_categories) {
            return true;
        } else if (itemId == R.id.nav_bookmarks) {
            navigateTo(BookmarksActivity.class);
            return true;
        } else if (itemId == R.id.nav_profile) {
            navigateToProfile();
            return true;
        }
        
        return false;
    }
    
    private void navigateToProfile() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            navigateTo(ProfileActivity.class);
        } else {
            navigateToSignIn();
        }
    }
    
    private void navigateToSignIn() {
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
        startActivity(intent);
    }
    
    private void navigateTo(Class<?> destinationClass) {
        Intent intent = new Intent(this, destinationClass);
        startActivity(intent);
    }
    
    private void updateCategories(List<Category> categories) {
        if (categoryAdapter != null && categories != null) {
            categoryAdapter.updateCategories(categories);
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
    
    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(this, NewsListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshIfNeeded();
        }
    }
} 