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

    /**
     * Constructs a BookmarksViewModel and initializes LiveData for bookmarked articles, loading state, and error messages.
     *
     * @param application the application context used to obtain the NewsRepository instance
     */
    public BookmarksViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        bookmarkedArticles = newsRepository.getBookmarkedArticles();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
    }

    /**
     * Returns a LiveData stream containing the list of bookmarked articles.
     *
     * @return LiveData representing the current list of bookmarked articles
     */
    public LiveData<List<Article>> getBookmarkedArticles() {
        return bookmarkedArticles;
    }

    /**
     * Returns a LiveData object representing the loading state for bookmarked articles.
     *
     * @return LiveData indicating whether bookmarked articles are currently being loaded
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing the current error message related to bookmarked articles.
     *
     * @return LiveData holding the error message string, or null if there is no error.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /****
     * Triggers loading of bookmarked articles from the repository.
     */
    public void loadBookmarkedArticles() {
        newsRepository.loadBookmarkedArticles();
    }
} 