package com.example.appdocbao.ui.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.example.appdocbao.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class BookmarksActivity extends AppCompatActivity implements BookmarkAdapter.OnBookmarkClickListener {

    private static final String TAG = "BookmarksActivity";
    private RecyclerView recyclerViewBookmarks;
    private BookmarkAdapter bookmarkAdapter;
    private ProgressBar progressBar;
    private TextView tvNoBookmarks;
    private View emptyStateContainer;
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
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set up RecyclerView
        recyclerViewBookmarks.setLayoutManager(new LinearLayoutManager(this));
        bookmarkAdapter = new BookmarkAdapter(this);
        recyclerViewBookmarks.setAdapter(bookmarkAdapter);

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
        bottomNavigationView.setOnItemSelectedListener(item -> {
            try {
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
            } catch (Exception e) {
                Log.e(TAG, "Error in navigation: " + e.getMessage(), e);
                return false;
            }
        });
    }

    private void updateBookmarks(List<Article> articles) {
        if (articles != null && !articles.isEmpty()) {
            Log.d(TAG, "Found " + articles.size() + " bookmarked articles");
            for (Article article : articles) {
                Log.d(TAG, "Bookmarked article: " + article.getId() + " - " + article.getTitle());
            }
            bookmarkAdapter.updateBookmarks(articles);
            tvNoBookmarks.setVisibility(View.GONE);
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.GONE);
            }
            recyclerViewBookmarks.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "No bookmarked articles found");
            recyclerViewBookmarks.setVisibility(View.GONE);
            tvNoBookmarks.setVisibility(View.VISIBLE);
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.VISIBLE);
            }
            // Show toast message to help user understand
            Toast.makeText(this, "Không tìm thấy bài viết đã lưu. Hãy lưu thêm bài viết!", Toast.LENGTH_LONG).show();
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
    public void onBookmarkClick(Article article) {
        Log.d(TAG, "Opening article: " + article.getId());
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(Constants.EXTRA_ARTICLE_ID, article.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Article article, int position) {
        Log.d(TAG, "Delete requested for: " + article.getId());
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài viết đã lưu")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này khỏi danh sách đã lưu?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Remove from database first
                    viewModel.removeBookmark(article.getId(), () -> {
                        // Then update UI
                        bookmarkAdapter.removeBookmark(position);
                        
                        // Show snackbar with undo option
                        Snackbar.make(recyclerViewBookmarks, "Đã xóa bài viết", Snackbar.LENGTH_LONG)
                                .setAction("Hoàn tác", v -> {
                                    // Add back to database
                                    viewModel.addBookmark(article);
                                    // Refresh the list
                                    viewModel.loadBookmarkedArticles();
                                })
                                .show();
                        
                        // Check if there are no more bookmarks
                        if (bookmarkAdapter.getItemCount() == 0) {
                            recyclerViewBookmarks.setVisibility(View.GONE);
                            tvNoBookmarks.setVisibility(View.VISIBLE);
                            if (emptyStateContainer != null) {
                                emptyStateContainer.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload bookmarks when returning to this activity
        viewModel.loadBookmarkedArticles();
    }
} 