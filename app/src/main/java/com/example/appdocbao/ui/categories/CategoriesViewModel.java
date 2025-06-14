package com.example.appdocbao.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.repository.CategoryRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {
    
    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> categories;
    private final LiveData<Boolean> isLoading;
    private long lastRefreshTime = 0;
    private static final long REFRESH_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = CategoryRepository.getInstance(application);
        categories = categoryRepository.getCategories();
        isLoading = categoryRepository.getIsLoading();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadCategories() {
        categoryRepository.loadCategories();
        lastRefreshTime = System.currentTimeMillis();
    }
    
    public void refreshCategories() {
        categoryRepository.loadCategories();
        lastRefreshTime = System.currentTimeMillis();
    }
    
    public void refreshIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastRefreshTime) > REFRESH_THRESHOLD_MS) {
            refreshCategories();
        }
    }
} 