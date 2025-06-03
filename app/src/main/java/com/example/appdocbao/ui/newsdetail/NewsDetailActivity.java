package com.example.appdocbao.ui.newsdetail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.utils.Constants;

public class NewsDetailActivity extends AppCompatActivity {

    private ImageView imgArticle;
    private TextView tvTitle, tvCategory, tvPublishedTime, tvSource, tvContent;
    private ImageButton btnBack, btnBookmark, btnShare;
    private ProgressBar progressBar;
    private NewsDetailViewModel viewModel;
    private String articleId;
    private boolean isBookmarked = false;

    /**
     * Initializes the activity to display detailed news article information.
     *
     * Retrieves the article ID from the intent, sets up UI components, initializes the ViewModel, and observes LiveData for article details, loading state, error messages, and bookmark status. Handles missing article ID and initialization errors by notifying the user and closing the activity.
     *
     * @param savedInstanceState the previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        try {
            // Get article ID from intent
            articleId = getIntent().getStringExtra(Constants.EXTRA_ARTICLE_ID);
            if (articleId == null) {
                Toast.makeText(this, "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize UI components
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            imgArticle = findViewById(R.id.ivNewsImage);
            tvTitle = findViewById(R.id.tvTitle);
            tvCategory = findViewById(R.id.tvCategory);
            tvPublishedTime = findViewById(R.id.tvTime);
            tvSource = findViewById(R.id.tvSource);
            tvContent = findViewById(R.id.tvContent);
            btnBack = findViewById(R.id.btnBack);
            btnBookmark = findViewById(R.id.btnBookmark);
            btnShare = findViewById(R.id.btnShare);
            progressBar = findViewById(R.id.progressBar);

            // Initialize ViewModel
            viewModel = new ViewModelProvider(this).get(NewsDetailViewModel.class);

            // Set click listeners
            btnBack.setOnClickListener(v -> onBackPressed());
            btnBookmark.setOnClickListener(v -> toggleBookmark());
            btnShare.setOnClickListener(v -> shareArticle());

            // Observe article data
            viewModel.getArticle().observe(this, this::updateArticleDetails);
            viewModel.getIsLoading().observe(this, this::setLoadingState);
            viewModel.getErrorMessage().observe(this, this::showError);
            viewModel.getIsBookmarked().observe(this, this::updateBookmarkState);

            // Load article details
            Log.d("NewsDetailActivity", "Loading article detail with ID: " + articleId);
            viewModel.loadArticleDetail(articleId);
            
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Updates the UI with the details of the provided article.
     *
     * Populates text fields with the article's title, category, source, published time, and content.
     * Loads the article image into the image view, using a placeholder if the image URL is missing or loading fails.
     * If the article is null, displays an error message to the user.
     *
     * @param article the Article object containing details to display
     */
    private void updateArticleDetails(Article article) {
        try {
            if (article != null) {
                Log.d("NewsDetailActivity", "Updating article details: " + article.getTitle());
                
                // Basic text fields first
                tvTitle.setText(article.getTitle());
                tvCategory.setText(getString(R.string.category) + " " + article.getCategoryName());
                tvSource.setText(getString(R.string.source) + " " + article.getSourceName());
                tvPublishedTime.setText(article.getPublishedTimeFormatted());
                
                // Set content - check for null to prevent crashes
                String content = article.getContent();
                if (content != null && !content.isEmpty()) {
                    tvContent.setText(content);
                } else {
                    tvContent.setText("Không có nội dung");
                }

                // Load article image
                String imageUrl = article.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Fix image URL if needed
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    
                    Log.d("NewsDetailActivity", "Loading detail image: " + imageUrl);
                    
                    // Use a try-catch to handle any Glide errors
                    try {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .centerCrop()
                            .into(imgArticle);
                    } catch (Exception e) {
                        Log.e("NewsDetailActivity", "Error loading image: " + e.getMessage(), e);
                        imgArticle.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    imgArticle.setImageResource(R.drawable.placeholder_image);
                    Log.d("NewsDetailActivity", "No image URL for article detail");
                }
            } else {
                Log.e("NewsDetailActivity", "Received null article in updateArticleDetails");
                Toast.makeText(this, "Không thể tải thông tin bài viết", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error updating article details: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi hiển thị bài viết", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the bookmark icon to reflect the current bookmark state.
     *
     * @param isBookmarked true if the article is bookmarked, false otherwise
     */
    private void updateBookmarkState(Boolean isBookmarked) {
        try {
            if (isBookmarked != null) {
                this.isBookmarked = isBookmarked;
                btnBookmark.setImageResource(isBookmarked ? 
                        R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error updating bookmark state: " + e.getMessage(), e);
        }
    }

    /**
     * Toggles the bookmark state of the current article.
     *
     * Adds or removes the article from bookmarks based on its current state.
     * Displays an error message if the operation fails.
     */
    private void toggleBookmark() {
        try {
            if (isBookmarked) {
                viewModel.removeBookmark(articleId);
            } else {
                viewModel.addBookmark(articleId);
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error toggling bookmark: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đánh dấu bài viết", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shares the current article's title and source URL using an Android share intent.
     *
     * Opens a chooser dialog allowing the user to share the article details via compatible apps.
     */
    private void shareArticle() {
        try {
            Article article = viewModel.getArticle().getValue();
            if (article != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, article.getTitle() + "\n\n" + article.getSourceUrl());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error sharing article: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi chia sẻ bài viết", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows or hides the progress bar based on the loading state.
     *
     * @param isLoading true to display the progress bar, false to hide it
     */
    private void setLoadingState(boolean isLoading) {
        try {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error setting loading state: " + e.getMessage(), e);
        }
    }

    /**
     * Displays an error message to the user as a toast and logs the error.
     *
     * @param errorMessage the error message to display; ignored if null or empty
     */
    private void showError(String errorMessage) {
        try {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("NewsDetailActivity", "Error message: " + errorMessage);
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error showing error message: " + e.getMessage(), e);
        }
    }
} 