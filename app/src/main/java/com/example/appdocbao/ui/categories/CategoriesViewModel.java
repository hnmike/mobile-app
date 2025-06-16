package com.example.appdocbao.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.repository.CategoryRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> categories;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = CategoryRepository.getInstance(application);
        categories = categoryRepository.getCategories();
        isLoading = categoryRepository.getIsLoading();
        errorMessage = categoryRepository.getErrorMessage();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadCategories() {
        categoryRepository.loadCategories();
    }
} 