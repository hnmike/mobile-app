package com.example.appdocbao.ui.bookmarks;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.local.BookmarkDbHelper;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.NewsRepository;

import java.util.List;

public class BookmarksViewModel extends AndroidViewModel {

    private static final String TAG = "BookmarksViewModel";
    private final NewsRepository newsRepository;
    private final LiveData<List<Article>> bookmarkedArticles;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;
    private final BookmarkDbHelper dbHelper;

    public BookmarksViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        bookmarkedArticles = newsRepository.getBookmarkedArticles();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
        dbHelper = BookmarkDbHelper.getInstance(application);
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
        Log.d(TAG, "Loading bookmarked articles");
        
        // For diagnostic purposes, also check directly in the DB 
        // to compare with what comes from the repository
        try {
            List<Article> directDbBookmarks = dbHelper.getAllBookmarks();
            Log.d(TAG, "Direct DB check found " + directDbBookmarks.size() + " bookmarks");
        } catch (Exception e) {
            Log.e(TAG, "Error in direct DB check: " + e.getMessage(), e);
        }
        
        // Load through repository (this updates the LiveData)
        newsRepository.loadBookmarkedArticles();
    }
    
    /**
     * Remove a bookmark from the database
     * @param articleId ID of the article to remove
     * @param onSuccess Callback to execute when successful
     */
    public void removeBookmark(String articleId, Runnable onSuccess) {
        Log.d(TAG, "Removing bookmark with ID: " + articleId);
        try {
            boolean result = dbHelper.removeBookmark(articleId);
            if (result) {
                Log.d(TAG, "Successfully removed bookmark from database");
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                Log.e(TAG, "Failed to remove bookmark from database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing bookmark: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a bookmark to the database
     * @param article Article to add
     */
    public void addBookmark(Article article) {
        if (article == null) {
            Log.e(TAG, "Cannot add null article to bookmarks");
            return;
        }
        
        Log.d(TAG, "Adding bookmark for article: " + article.getId());
        try {
            article.setBookmarked(true);
            boolean result = dbHelper.saveBookmark(article);
            if (result) {
                Log.d(TAG, "Successfully added bookmark to database");
            } else {
                Log.e(TAG, "Failed to add bookmark to database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding bookmark: " + e.getMessage(), e);
        }
    }
} 