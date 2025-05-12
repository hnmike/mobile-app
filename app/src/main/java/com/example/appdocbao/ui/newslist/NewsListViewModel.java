package com.example.appdocbao.ui.newslist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.NewsRepository;

import java.util.List;

public class NewsListViewModel extends AndroidViewModel {

    private final NewsRepository newsRepository;
    private final LiveData<List<Article>> articles;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    public NewsListViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        articles = newsRepository.getArticles();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
    }

    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadArticlesByCategory(String categoryId) {
        newsRepository.loadArticlesByCategory(categoryId);
    }
} 