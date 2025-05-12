package com.example.appdocbao.ui.bookmarks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.NewsRepository;

import java.util.List;

public class BookmarksViewModel extends AndroidViewModel {

    private final NewsRepository newsRepository;
    private final LiveData<List<Article>> bookmarkedArticles;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    public BookmarksViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        bookmarkedArticles = newsRepository.getBookmarkedArticles();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
    }

    public LiveData<List<Article>> getBookmarkedArticles() {
        return bookmarkedArticles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadBookmarkedArticles() {
        newsRepository.loadBookmarkedArticles();
    }
} 