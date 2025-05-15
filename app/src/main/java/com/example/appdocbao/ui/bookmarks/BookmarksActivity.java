package com.example.appdocbao.ui.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.ui.newslist.NewsAdapter;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.example.appdocbao.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity implements NewsAdapter.OnArticleClickListener {

    private RecyclerView recyclerViewBookmarks;
    private NewsAdapter newsAdapter;
    private ProgressBar progressBar;
    private TextView tvNoBookmarks;
    private BookmarksViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.bookmarks));

        // Initialize UI components
        recyclerViewBookmarks = findViewById(R.id.recyclerViewBookmarks);
        progressBar = findViewById(R.id.progressBar);
        tvNoBookmarks = findViewById(R.id.tvNoBookmarks);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set up RecyclerView
        recyclerViewBookmarks.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(new ArrayList<>(), this);
        recyclerViewBookmarks.setAdapter(newsAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BookmarksViewModel.class);

        // Observe data changes
        viewModel.getBookmarkedArticles().observe(this, this::updateBookmarks);
        viewModel.getIsLoading().observe(this, this::setLoadingState);
        viewModel.getErrorMessage().observe(this, this::showError);

        // Set up bottom navigation
        setupBottomNavigation();

        // Load bookmarked articles
        viewModel.loadBookmarkedArticles();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_bookmarks);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(BookmarksActivity.this, com.example.appdocbao.ui.home.HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    startActivity(new Intent(BookmarksActivity.this, CategoriesActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_bookmarks) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(BookmarksActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateBookmarks(List<Article> articles) {
        if (articles != null && !articles.isEmpty()) {
            newsAdapter.updateArticles(articles);
            tvNoBookmarks.setVisibility(View.GONE);
            recyclerViewBookmarks.setVisibility(View.VISIBLE);
        } else {
            recyclerViewBookmarks.setVisibility(View.GONE);
            tvNoBookmarks.setVisibility(View.VISIBLE);
        }
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(Constants.EXTRA_ARTICLE_ID, article.getId());
        startActivity(intent);
    }
} 