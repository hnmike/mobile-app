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

    /**
     * Initializes the CategoriesActivity, setting up the UI, RecyclerView, ViewModel, bottom navigation, and triggers loading of news categories.
     *
     * @param savedInstanceState the previously saved instance state, if any
     */
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
        } catch (Exception e) {
            Log.e(TAG, "Error initializing CategoriesActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing application: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Initializes and assigns UI components for the categories screen, including the RecyclerView, ProgressBar, and BottomNavigationView if present.
     */
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
    
    /**
     * Configures the RecyclerView to display categories in a two-column grid layout with a CategoryAdapter.
     *
     * Initializes the RecyclerView's layout manager and sets a new CategoryAdapter with an empty category list and this activity as the click listener.
     */
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
    
    /**
     * Initializes the CategoriesViewModel and sets up observers for category data, loading state, and error messages.
     *
     * Observers update the UI in response to changes in the categories list, loading progress, or error conditions.
     */
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

    /**
     * Configures the bottom navigation bar to handle navigation between Home, Categories, Bookmarks, and Profile or SignIn screens.
     *
     * If the bottom navigation view is not present in the layout, the method exits without action. Navigation to the Profile screen checks the user's login status and directs to either the Profile or SignIn activity accordingly.
     */
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
     * Determines whether a user is currently logged in via Firebase Authentication.
     *
     * @return true if a Firebase user is logged in; false otherwise
     */
    private boolean isUserLoggedIn() {
        try {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Updates the displayed list of categories in the adapter.
     *
     * @param categories the new list of categories to display
     */
    private void updateCategories(List<Category> categories) {
        try {
            if (categoryAdapter != null && categories != null) {
                categoryAdapter.updateCategories(categories);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating categories: " + e.getMessage(), e);
        }
    }

    /**
     * Shows or hides the progress bar based on the loading state.
     *
     * @param isLoading true to display the progress bar, false to hide it
     */
    private void setLoadingState(boolean isLoading) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting loading state: " + e.getMessage(), e);
        }
    }

    /**
     * Displays a toast message with the provided error message if it is not empty.
     *
     * @param errorMessage the error message to display
     */
    private void showError(String errorMessage) {
        try {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the event when a category is selected by navigating to the news list for that category.
     *
     * @param category the selected category
     */
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