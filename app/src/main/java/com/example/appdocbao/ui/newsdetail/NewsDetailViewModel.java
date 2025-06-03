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

    /**
     * Initializes the NewsDetailViewModel and sets up LiveData fields for article details, loading state, error messages, and bookmark status.
     *
     * @param application the application context used to initialize the repository
     */
    public NewsDetailViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        article = newsRepository.getSelectedArticle();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
        isBookmarked = newsRepository.getIsArticleBookmarked();
    }

    /**
     * Returns a LiveData object containing the currently selected article.
     *
     * @return LiveData representing the selected Article, or null if not loaded
     */
    public LiveData<Article> getArticle() {
        return article;
    }

    /**
     * Returns a LiveData indicating whether the article detail is currently loading.
     *
     * @return LiveData that is true if loading is in progress, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData stream containing error messages related to news article operations.
     *
     * @return LiveData emitting error message strings, or null if no error has occurred
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns a LiveData indicating whether the current article is bookmarked.
     *
     * @return LiveData that emits true if the article is bookmarked, false otherwise
     */
    public LiveData<Boolean> getIsBookmarked() {
        return isBookmarked;
    }

    /**
     * Requests loading of detailed information for the specified article.
     *
     * @param articleId the unique identifier of the article to load
     */
    public void loadArticleDetail(String articleId) {
        newsRepository.loadArticleDetail(articleId);
    }

    /**
     * Adds a bookmark for the specified article.
     *
     * @param articleId the unique identifier of the article to bookmark
     */
    public void addBookmark(String articleId) {
        newsRepository.addBookmark(articleId);
    }

    /**
     * Removes the bookmark for the specified article.
     *
     * @param articleId the unique identifier of the article to unbookmark
     */
    public void removeBookmark(String articleId) {
        newsRepository.removeBookmark(articleId);
    }
} 