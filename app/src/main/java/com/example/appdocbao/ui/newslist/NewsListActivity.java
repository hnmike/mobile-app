package com.example.appdocbao.ui.newslist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NewsListActivity extends AppCompatActivity implements NewsAdapter.OnArticleClickListener {

    private RecyclerView recyclerViewNews;
    private NewsAdapter newsAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoArticles;
    private NewsListViewModel viewModel;
    private String categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        // Get category information from intent
        categoryId = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_NAME);

        if (categoryId == null || categoryName == null) {
            Toast.makeText(this, "Invalid category information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize UI components
        recyclerViewNews = findViewById(R.id.recyclerViewNews);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvNoArticles = findViewById(R.id.tvNoArticles);

        // Set up RecyclerView
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(new ArrayList<>(), this);
        recyclerViewNews.setAdapter(newsAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(NewsListViewModel.class);

        // Observe data changes
        viewModel.getArticles().observe(this, this::updateArticles);
        viewModel.getIsLoading().observe(this, this::setLoadingState);
        viewModel.getErrorMessage().observe(this, this::showError);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadArticlesByCategory(categoryId);
        });

        // Load articles
        viewModel.loadArticlesByCategory(categoryId);
    }

    private void updateArticles(List<Article> articles) {
        swipeRefreshLayout.setRefreshing(false);
        
        if (articles != null && !articles.isEmpty()) {
            newsAdapter.updateArticles(articles);
            tvNoArticles.setVisibility(View.GONE);
            recyclerViewNews.setVisibility(View.VISIBLE);
        } else {
            recyclerViewNews.setVisibility(View.GONE);
            tvNoArticles.setVisibility(View.VISIBLE);
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String errorMessage) {
        swipeRefreshLayout.setRefreshing(false);
        
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