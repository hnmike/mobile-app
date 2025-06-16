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
import com.example.appdocbao.ui.home.HomeActivity;
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
        
        // Khởi tạo views
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        
        // Thiết lập RecyclerView
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(this, 2));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        recyclerViewCategories.setAdapter(categoryAdapter);
        
        // Khởi tạo ViewModel và observe dữ liệu
        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        viewModel.getCategories().observe(this, this::updateCategories);
        viewModel.getIsLoading().observe(this, isLoading -> 
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        // Thiết lập bottom navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_categories);
            bottomNavigationView.setOnItemSelectedListener(this::handleNavigationItemSelected);
        }
        
        // Tải dữ liệu
        viewModel.loadCategories();
    }
    
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        } else if (itemId == R.id.nav_categories) {
            return true;
        } else if (itemId == R.id.nav_bookmarks) {
            startActivity(new Intent(this, BookmarksActivity.class));
            return true;
        } else if (itemId == R.id.nav_profile) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                Intent intent = new Intent();
                intent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                startActivity(intent);
            }
            return true;
        }
        
        return false;
    }
    
    private void updateCategories(List<Category> categories) {
        if (categories != null) {
            categoryAdapter.updateCategories(categories);
        }
    }
    
    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(this, NewsListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
        startActivity(intent);
    }
}
