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

    /**
     * Initializes the NewsListViewModel with application context and sets up LiveData for articles, loading state, and error messages from the NewsRepository.
     *
     * @param application the Application context used to initialize the repository
     */
    public NewsListViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        articles = newsRepository.getArticles();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
    }

    /**
     * Returns a LiveData list of news articles for observation by the UI.
     *
     * @return LiveData containing the current list of Article objects
     */
    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    /**
     * Returns a LiveData representing the loading state of news data operations.
     *
     * @return LiveData that is true when data is being loaded, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing the current error message related to news data operations.
     *
     * @return LiveData holding the error message string, or null if there is no error
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /****
     * Requests loading of news articles filtered by the specified category ID.
     *
     * @param categoryId the identifier of the news category to filter articles by
     */
    public void loadArticlesByCategory(String categoryId) {
        newsRepository.loadArticlesByCategory(categoryId);
    }
} 