package com.example.appdocbao.ui.newsdetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.NewsRepository;

public class NewsDetailViewModel extends AndroidViewModel {

    private final NewsRepository newsRepository;
    private final LiveData<Article> article;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;
    private final LiveData<Boolean> isBookmarked;

    public NewsDetailViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        article = newsRepository.getSelectedArticle();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
        isBookmarked = newsRepository.getIsArticleBookmarked();
    }

    public LiveData<Article> getArticle() {
        return article;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsBookmarked() {
        return isBookmarked;
    }

    public void loadArticleDetail(String articleId) {
        newsRepository.loadArticleDetail(articleId);
    }

    public void addBookmark(String articleId) {
        newsRepository.addBookmark(articleId);
    }

    public void removeBookmark(String articleId) {
        newsRepository.removeBookmark(articleId);
    }
} 