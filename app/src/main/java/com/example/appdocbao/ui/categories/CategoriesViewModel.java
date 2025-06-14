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
    }
} 