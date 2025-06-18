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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        try {
            // Get article ID from intent - hỗ trợ cả ID từ News object và Article object
            articleId = getIntent().getStringExtra(Constants.EXTRA_ARTICLE_ID);
            
            // Nếu không tìm thấy qua EXTRA_ARTICLE_ID, thử kiểm tra xem có ID dạng số nguyên không
            if (articleId == null) {
                int newsId = getIntent().getIntExtra("NEWS_ID", -1);
                if (newsId != -1) {
                    articleId = String.valueOf(newsId);
                    Log.d("NewsDetailActivity", "Using numeric ID: " + articleId);
                }
            }
            
            // Nếu vẫn không tìm thấy ID, kết thúc activity
            if (articleId == null) {
                Toast.makeText(this, "Không thể tải bài viết - ID không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d("NewsDetailActivity", "Article ID to load: " + articleId);
            
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

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if we need to update article information when internet is restored
        if (articleId != null) {
            viewModel.checkAndUpdateArticleInfo(articleId);
        }
    }

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

    private void updateBookmarkState(Boolean isBookmarked) {
        try {
            if (isBookmarked != null) {
                this.isBookmarked = isBookmarked;
                btnBookmark.setImageResource(isBookmarked ? 
                        R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
                Log.d("NewsDetailActivity", "Updated bookmark state to: " + isBookmarked + " for article: " + articleId);
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error updating bookmark state: " + e.getMessage(), e);
        }
    }

    private void toggleBookmark() {
        try {
            Log.d("NewsDetailActivity", "Toggle bookmark for article: " + articleId + ", current state: " + isBookmarked);
            
            boolean newBookmarkState = viewModel.toggleBookmark(articleId);
            isBookmarked = newBookmarkState;
            updateBookmarkState(newBookmarkState);
            
            if (newBookmarkState) {
                Toast.makeText(this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã xóa bài viết khỏi danh sách đã lưu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error toggling bookmark: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đánh dấu bài viết", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareArticle() {
        try {
            Article article = viewModel.getArticle().getValue();
            if (article != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
                
                // Build share text with fallback for missing source URL
                String shareText = article.getTitle();
                String sourceUrl = article.getSourceUrl();
                if (sourceUrl != null && !sourceUrl.isEmpty()) {
                    shareText += "\n\n" + sourceUrl;
                } else {
                    // Fallback: use a generic VnExpress URL
                    shareText += "\n\nhttps://vnexpress.net";
                }
                
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            } else {
                Toast.makeText(this, "Không thể chia sẻ bài viết lúc này", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error sharing article: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi chia sẻ bài viết", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoadingState(boolean isLoading) {
        try {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e("NewsDetailActivity", "Error setting loading state: " + e.getMessage(), e);
        }
    }

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