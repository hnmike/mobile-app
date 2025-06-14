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

    private void initializeComponents() {
        initViews();
        setupRecyclerView();
        initViewModel();
        setupBottomNavigation();
        loadData();
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
            bottomNavigationView.setOnItemSelectedListener(this::handleNavigationItemSelected);
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